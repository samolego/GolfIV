package org.samo_lego.golfiv.casts;

import net.minecraft.block.Block;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.potion.PotionUtil;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.Collections;

/**
 * Checks iItemStacks.
 */
public interface ItemStackChecker {
    /**
     * NBT Key {@code BlockEntityTag} for {@link BlockItem BlockItems} containing block entities.
     */
    String BLOCK_ENTITY_TAG_KEY = "BlockEntityTag";

    /**
     * Cached unluck singleton
     */
    Collection<StatusEffectInstance> UNLUCK = Collections.singleton(new StatusEffectInstance(StatusEffects.UNLUCK, 6000));

    /**
     * Checks the ItemStack and makes it legal.
     * Removes all enchantments if they are incompatible or
     * have too high level.
     */
    void makeLegal(boolean survival);

    /**
     * Creates a fake ItemStack based on the
     * provided stack.
     * <p>
     * E.g. removes enchantment info, stack size, etc.
     * Used on ground ItemEntities / opponent's stacks etc.
     *
     * @param original   the original ItemStack
     * @param spoofCount whether or not the item count should be faked
     * @return the faked ItemStack
     */
    static ItemStack fakeStack(ItemStack original, boolean spoofCount) {
        ItemStack fakedStack = spoofCount ?
                new ItemStack(original.getItem(), original.getMaxCount()) :
                new ItemStack(original.getItem(), original.getCount());

        if (original.hasEnchantments())
            fakedStack.addEnchantment(null, 0);

        Item item = original.getItem();
        if (item instanceof DyeableItem dyeable) {
            if (dyeable.hasColor(original)) {
                dyeable.setColor(fakedStack, dyeable.getColor(original));
            }
        }

        if (item instanceof PotionItem || item instanceof TippedArrowItem) {
            // Lets dropping potions and arrows to be less of a 'surprising' change.
            fakedStack.setSubNbt(PotionUtil.CUSTOM_POTION_COLOR_KEY, NbtInt.of(PotionUtil.getColor(original)));
            if (item.hasGlint(original)) {
                PotionUtil.setCustomPotionEffects(fakedStack, UNLUCK);
            }
        }

        if (item instanceof WritableBookItem || item instanceof WrittenBookItem) {
            // Prevents issues with other mods expecting pages to be present.
            fakedStack.setSubNbt("pages", new NbtList());
        }

        if (item instanceof BannerItem) {
            // Lets dropping banners not be a surprising change, and for illager patrol leaders to have a proper banner.
            cleanBanner(original.getSubNbt(BLOCK_ENTITY_TAG_KEY), fakedStack);
        }

        if (item instanceof SkullItem) {
            // Lets dropping player heads not be a surprising change, and for wearing heads to show the actual skin.
            cleanSkull(original.getSubNbt(SkullItem.SKULL_OWNER_KEY), fakedStack);
        }

        if (item instanceof CrossbowItem && CrossbowItem.isCharged(original)) {
            cleanCrossbow(original.getNbt(), fakedStack, false);
        }

        return fakedStack;
    }

    /**
     * This is for keeping creative from getting inadvertently sanitised,
     * as creative has the property of allowing one to summon any item at will,
     * including freely mutating the inventory.
     *
     * @param stack The original ItemStack.
     * @return The faked ItemStack, with a GolfIV pointer injected.
     * @author KJP12
     * @see #inventoryStack(ItemStack)
     */
    static ItemStack creativeInventoryStack(ItemStack stack) {
        ItemStack fake = inventoryStack(stack);
        if (stack.hasNbt()) //noinspection ConstantConditions
            fake.setSubNbt("GolfIV", NbtInt.of(stack.getNbt().hashCode()));
        return fake;
    }

    /**
     * Creates a fake ItemStack based on the
     * provided stack.
     * <p>
     * Changes the tag to be the minimal required
     * NBT to render in an inventory.
     *
     * @param stack The original ItemStack.
     * @return The faked ItemStack
     */
    static ItemStack inventoryStack(ItemStack stack) {
        // TODO: Perhaps take a more dynamic approach to this?
        //  This is not really flexible as is and may leave modded items broken.
        NbtCompound tag = stack.getNbt();
        if (tag == null || tag.isEmpty()) {
            // Sanitization isn't necessary when it's already empty.
            return stack;
        }

        Item item = stack.getItem();
        ItemStack fake = new ItemStack(item, stack.getCount());

        // Rewrite display.
        if (tag.contains(ItemStack.DISPLAY_KEY)) {
            NbtCompound display = tag.getCompound(ItemStack.DISPLAY_KEY);
            NbtCompound fakeDisplay = new NbtCompound();
            NbtElement name = display.get(ItemStack.NAME_KEY);
            if (name != null) fakeDisplay.put(ItemStack.NAME_KEY, name);
            if (display.contains(ItemStack.COLOR_KEY))
                fakeDisplay.put(ItemStack.COLOR_KEY, display.get(ItemStack.COLOR_KEY));
            NbtElement lore = display.get(ItemStack.LORE_KEY);
            if (lore != null) fakeDisplay.put(ItemStack.LORE_KEY, lore);
            fake.setSubNbt(ItemStack.DISPLAY_KEY, fakeDisplay);
        }

        // Rewrite enchantments.
        if(stack.hasEnchantments()) {
            NbtList enchants = new NbtList();
            final String ID_KEY = "id";
            final String LVL_KEY = "lvl";
            for(NbtElement enchant : stack.getEnchantments()) {
                if(enchant instanceof NbtCompound compound) {
                    String id = compound.getString(ID_KEY);
                    int lvl = compound.getInt(LVL_KEY);
                    if (Registries.ENCHANTMENT.containsId(new Identifier(id))) {
                        NbtCompound minimalEnchant = new NbtCompound();
                        minimalEnchant.putString(ID_KEY, id);
                        minimalEnchant.putInt(LVL_KEY, lvl);
                        enchants.add(minimalEnchant);
                    }
                }
            }
            fake.setSubNbt(ItemStack.ENCHANTMENTS_KEY, enchants);
        }

        // Rewrite damage if it is damageable.
        if(stack.isDamageable()) {
            fake.setDamage(stack.getDamage());
        }

        // Check block items.
        if(item instanceof BlockItem) {
            boolean flag = true;
            if (item instanceof BannerItem) {
                flag = false;
                cleanBanner(stack.getSubNbt(BLOCK_ENTITY_TAG_KEY), fake);
            }

            // Transfer SkullProperties.
            if (item instanceof SkullItem) {
                flag = false;
                cleanSkull(stack.getSubNbt(SkullItem.SKULL_OWNER_KEY), fake);
            }

            if (flag) {
                Block block = ((BlockItem) item).getBlock();

                // Rewrite shulker items
                if (block instanceof ShulkerBoxBlock) {
                    NbtCompound blockEntity = stack.getSubNbt(BLOCK_ENTITY_TAG_KEY);
                    if (blockEntity != null) {
                        NbtCompound fakeEntity = fake.getOrCreateSubNbt(BLOCK_ENTITY_TAG_KEY);
                        if (blockEntity.contains("LootTable", NbtElement.STRING_TYPE)) {
                            fakeEntity.put("LootTable", blockEntity.get("LootTable"));
                        }
                        if (blockEntity.contains("Items", NbtElement.LIST_TYPE)) {
                            NbtList fakeItems = new NbtList();
                            for (NbtElement $item : blockEntity.getList("Items", NbtElement.COMPOUND_TYPE)) {
                                if ($item == null) continue;
                                NbtCompound oldItem = (NbtCompound) $item;
                                NbtCompound fakeItem = new NbtCompound();
                                if (oldItem.contains("Slot", NbtElement.BYTE_TYPE))
                                    fakeItem.put("Slot", oldItem.get("Slot"));
                                if (oldItem.contains("id", NbtElement.STRING_TYPE))
                                    fakeItem.put("id", oldItem.get("id"));
                                if (oldItem.contains("Count", NbtElement.BYTE_TYPE))
                                    fakeItem.put("Count", oldItem.get("Count"));
                                // TODO: Add in display name
                                fakeItems.add(fakeItem);
                            }
                            fakeEntity.put("Items", fakeItems);
                        }
                    }
                }
            }
        }

        // Rewrite potion effects.
        if(item instanceof PotionItem || item instanceof TippedArrowItem) {
            if(tag.contains("Potion", NbtElement.STRING_TYPE)) fake.setSubNbt("Potion", tag.get("Potion"));
            if(tag.contains("CustomPotionColor", NbtElement.INT_TYPE)) fake.setSubNbt("CustomPotionColor", tag.get("CustomPotionColor"));
            if(tag.contains("CustomPotionEffects", NbtElement.LIST_TYPE)) {
                NbtList fakeEffects = new NbtList();
                for(NbtElement effect : tag.getList("CustomPotionEffects", NbtElement.COMPOUND_TYPE)) {
                    if(effect == null) continue;
                    NbtCompound oldEffect = (NbtCompound) effect;
                    NbtCompound fakeEffect = new NbtCompound();
                    if(oldEffect.contains("Id", NbtElement.STRING_TYPE)) fakeEffect.put("Id", oldEffect.get("Id"));
                    if(oldEffect.contains("Amplifier", NbtElement.BYTE_TYPE)) fakeEffect.put("Amplifier", oldEffect.get("Amplifier"));
                    if(oldEffect.contains("Duration", NbtElement.INT_TYPE)) fakeEffect.put("Duration", oldEffect.get("Duration"));
                    fakeEffects.add(fakeEffect);
                }
                fake.setSubNbt("CustomPotionEffects", fakeEffects);
            }
        }

        // Brokenly rewrites written books.
        if(item instanceof WrittenBookItem) {
            if(tag.contains("title", NbtElement.STRING_TYPE)) fake.setSubNbt("title", tag.get("title"));
            if(tag.contains("author", NbtElement.STRING_TYPE)) fake.setSubNbt("author", tag.get("author"));
            if(tag.contains("generation", NbtElement.INT_TYPE)) fake.setSubNbt("generation", tag.get("generation"));
            // FIXME: Pages need to be present for the book to work. Force update on selection?
            // Prevents issues with other mods expecting pages to be present.
            fake.setSubNbt("pages", new NbtList());
        }

        if (item instanceof WritableBookItem) {
            // FIXME: Pages need to be present for the book to work. Force update on selection?
            // Prevents issues with other mods expecting pages to be present.
            fake.setSubNbt("pages", new NbtList());
        }

        if (item instanceof FilledMapItem) {
            fake.getOrCreateNbt().putInt("map", tag.getInt("map"));
        }

        if (item instanceof CrossbowItem && CrossbowItem.isCharged(stack)) {
            cleanCrossbow(stack.getNbt(), fake, true);
        }

        return fake;
    }

    /**
     * Minimally copies over the crossbow data based on the provided Crossbow data.
     * <p>
     * Only the NBT required to render the crossbow, including rocket, is copied over.
     *
     * @param crossbow    The raw crossbow NBT.
     * @param faked       The faked ItemStack to copy to.
     * @param isInventory Whether to send the raw item or not.
     */
    static void cleanCrossbow(NbtCompound crossbow, ItemStack faked, boolean isInventory) {
        if (crossbow == null) return;
        NbtList originalProjectiles = crossbow.getList("ChargedProjectiles", NbtElement.COMPOUND_TYPE);
        if (originalProjectiles.isEmpty()) return;
        String originalProjectile = originalProjectiles.getCompound(0).getString("id");
        String projectile;

        if (isInventory || "minecraft:firework".equals(originalProjectile)) {
            projectile = originalProjectile;
        } else {
            projectile = "minecraft:arrow";
        }

        NbtCompound projectileStack = new NbtCompound();
        projectileStack.putByte("Count", (byte) 1);
        projectileStack.putString("id", projectile);
        NbtList projectiles = new NbtList();
        projectiles.add(projectileStack);
        faked.setSubNbt("ChargedProjectiles", projectiles);
        faked.setSubNbt("Charged", NbtByte.of(true));
    }

    /**
     * Minimally copies over the skull data based on the provided SkullOwner data.
     * <p>
     * Only the NBT required to render the skull is copied over.
     *
     * @param skullOwner The SkullOwner NBT compound. May be null.
     * @param faked      The faked ItemStack to copy to.
     */
    static void cleanSkull(NbtCompound skullOwner, ItemStack faked) {
        if (skullOwner != null) {
            NbtCompound fakeSkullOwner = faked.getOrCreateSubNbt(SkullItem.SKULL_OWNER_KEY);
            if (skullOwner.containsUuid("Id")) fakeSkullOwner.putUuid("Id", skullOwner.getUuid("Id"));
            if (skullOwner.contains("Properties", NbtElement.COMPOUND_TYPE)) {
                NbtCompound skullProperties = skullOwner.getCompound("Properties");
                if (skullProperties.contains("textures", NbtElement.LIST_TYPE)) {
                    NbtList skullTextures = skullProperties.getList("textures", NbtElement.COMPOUND_TYPE);
                    if (!skullTextures.isEmpty()) {
                        NbtCompound skullValueContainer = skullTextures.getCompound(0);
                        String skullTexture = skullValueContainer.getString("Value");
                        NbtCompound fakeProperties = new NbtCompound();
                        NbtList fakeTextures = new NbtList();
                        NbtCompound fakeTexture = new NbtCompound();
                        fakeTexture.putString("Value", skullTexture);
                        fakeTextures.add(fakeTexture);
                        fakeProperties.put("textures", fakeTextures);
                        fakeSkullOwner.put("Properties", fakeProperties);
                    }
                }
            }
        }
    }

    /**
     * Minimally copies over the banner NBT based on the provided blockEntity data.
     * <p>
     * Only the NBT required to render a layer is copied over.
     *
     * @param blockEntity The original block entity tag. May be null.
     * @param faked       The faked ItemStack to copy to.
     */
    static void cleanBanner(NbtCompound blockEntity, ItemStack faked) {
        if (blockEntity != null && blockEntity.contains("Patterns", NbtElement.LIST_TYPE)) {
            NbtList fakePatterns = new NbtList();
            int i = 0;
            for(NbtElement pattern : blockEntity.getList("Patterns", NbtElement.COMPOUND_TYPE)) {
                if(!(pattern instanceof NbtCompound oldPattern)) continue;
                // A 12 layer cap on the rewriting is primarily to prevent
                // the faked stack from becoming the kicking item.
                if(i++ >= 12) break;
                if (oldPattern.contains("Color", NbtElement.INT_TYPE) &&
                        oldPattern.contains("Pattern", NbtElement.STRING_TYPE)) {
                    if(oldPattern.getSize() == 2) {
                        fakePatterns.add(oldPattern);
                    } else {
                        NbtCompound fakePattern = new NbtCompound();
                        fakePattern.put("Color", oldPattern.get("Color"));
                        fakePattern.put("Pattern", oldPattern.get("Pattern"));
                        fakePatterns.add(fakePattern);
                    }
                }
            }
            faked.getOrCreateSubNbt(BLOCK_ENTITY_TAG_KEY).put("Patterns", fakePatterns);
        }
    }
}
