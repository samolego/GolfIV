package org.samo_lego.golfiv.casts;

import net.minecraft.block.Block;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

/**
 * Checks iItemStacks.
 */
public interface ItemStackChecker {
    /**
     * Checks the ItemStack and makes it legal.
     * Removes all enchantments if they are incompatible or
     * have too high level.
     */
    void makeLegal(boolean survival);

    /**
     * Creates a fake ItemStack based on the
     * provided stack.
     *
     * E.g. removes enchantment info, stack size, etc.
     * Used on ground ItemEntities / opponent's stacks etc.
     *
     * @param original the original ItemStack
     * @param spoofCount whether or not the item count should be faked
     * @return the faked ItemStack
     */
    static ItemStack fakeStack(ItemStack original, boolean spoofCount) {
        ItemStack fakedStack = spoofCount ?
                new ItemStack(original.getItem(), original.getMaxCount()) :
                new ItemStack(original.getItem(), original.getCount());

        if(original.hasEnchantments())
            fakedStack.addEnchantment(null, 0);

        Item item = original.getItem();
        if(item instanceof PotionItem || item instanceof TippedArrowItem) {
            // Lets dropping potions and arrows to be less of a 'surprising' change.
            fakedStack.getOrCreateTag().putInt("CustomPotionColor", PotionUtil.getColor(original));
        }

        if(item instanceof WritableBookItem || item instanceof WrittenBookItem) {
            // Prevents issues with other mods expecting pages to be present.
            fakedStack.putSubTag("pages", new NbtList());
        }

        if(item instanceof BannerItem) {
            // Lets dropping banners not be a surprising change, and for illager patrol leaders to have a proper banner.
            cleanBanner(original.getSubTag("BlockEntityTag"), fakedStack);
        }

        return fakedStack;
    }

    /**
     * Creates a fake ItemStack based on the
     * provided stack.
     *
     * Changes the tag to be the minimal required
     * NBT to render in an inventory.
     *
     * @param stack The original ItemStack.
     * @return The faked ItemStack
     * */
    static ItemStack inventoryStack(ItemStack stack) {
        // TODO: Perhaps take a more dynamic approach to this?
        //  This is not really flexible as is and may leave modded items broken.
        NbtCompound tag = stack.getTag();
        if(tag == null || tag.isEmpty()) {
            // Sanitization isn't necessary when it's already empty.
            return stack;
        }

        Item item = stack.getItem();
        ItemStack fake = new ItemStack(item, stack.getCount());

        // Rewrite display.
        if(tag.contains(ItemStack.DISPLAY_KEY)) {
            NbtCompound display = tag.getCompound(ItemStack.DISPLAY_KEY);
            NbtCompound fakeDisplay = new NbtCompound();
            NbtElement name = display.get(ItemStack.NAME_KEY);
            if(name != null) fakeDisplay.put(ItemStack.NAME_KEY, name);
            if(display.contains(ItemStack.COLOR_KEY)) fakeDisplay.put(ItemStack.COLOR_KEY, display.get(ItemStack.COLOR_KEY));
            NbtElement lore = display.get(ItemStack.LORE_KEY);
            if(lore != null) fakeDisplay.put(ItemStack.LORE_KEY, lore);
            fake.putSubTag(ItemStack.DISPLAY_KEY, fakeDisplay);
        }

        // Rewrite enchantments.
        if(stack.hasEnchantments()) {
            NbtList enchants = new NbtList();
            for(NbtElement enchant : stack.getEnchantments()) {
                if(enchant instanceof NbtCompound) {
                    NbtCompound compound = (NbtCompound) enchant;
                    String id = compound.getString(ItemStack.ID_KEY);
                    int lvl = compound.getInt(ItemStack.LVL_KEY);
                    if(Registry.ENCHANTMENT.containsId(new Identifier(id))) {
                        NbtCompound minimalEnchant = new NbtCompound();
                        minimalEnchant.putString(ItemStack.ID_KEY, id);
                        minimalEnchant.putInt(ItemStack.LVL_KEY, lvl);
                        enchants.add(minimalEnchant);
                    }
                }
            }
            fake.putSubTag(ItemStack.ENCHANTMENTS_KEY, enchants);
        }

        // Rewrite damage if it is damageable.
        if(stack.isDamageable()) {
            fake.setDamage(stack.getDamage());
        }

        // Check block items.
        if(item instanceof BlockItem) {
            boolean flag = true;
            if(item instanceof BannerItem) {
                flag = false;
                cleanBanner(stack.getSubTag("BlockEntityTag"), fake);
            }

            if(flag) {
                Block block = ((BlockItem) item).getBlock();

                // Rewrite shulker items
                if (block instanceof ShulkerBoxBlock) {
                    NbtCompound blockEntity = stack.getSubTag("BlockEntityTag");
                    if(blockEntity != null) {
                        NbtCompound fakeEntity = fake.getOrCreateSubTag("BlockEntityTag");
                        if(blockEntity.contains("LootTable", NbtElement.STRING_TYPE)) {
                            fakeEntity.put("LootTable", blockEntity.get("LootTable"));
                        }
                        if(blockEntity.contains("Items", NbtElement.LIST_TYPE)) {
                            NbtList fakeItems = new NbtList();
                            for(NbtElement $item : blockEntity.getList("Items", NbtElement.COMPOUND_TYPE)) {
                                if($item == null) continue;
                                NbtCompound oldItem = (NbtCompound) $item;
                                NbtCompound fakeItem = new NbtCompound();
                                if(oldItem.contains("Slot", NbtElement.BYTE_TYPE)) fakeItem.put("Slot", oldItem.get("Slot"));
                                if(oldItem.contains("id", NbtElement.STRING_TYPE)) fakeItem.put("id", oldItem.get("id"));
                                if(oldItem.contains("Count", NbtElement.BYTE_TYPE)) fakeItem.put("Count", oldItem.get("Count"));
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
            if(tag.contains("Potion", NbtElement.STRING_TYPE)) fake.putSubTag("Potion", tag.get("Potion"));
            if(tag.contains("CustomPotionColor", NbtElement.INT_TYPE)) fake.putSubTag("CustomPotionColor", tag.get("CustomPotionColor"));
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
                fake.putSubTag("CustomPotionEffects", fakeEffects);
            }
        }

        // Brokenly rewrites written books.
        if(item instanceof WrittenBookItem) {
            if(tag.contains("title", NbtElement.STRING_TYPE)) fake.putSubTag("title", tag.get("title"));
            if(tag.contains("author", NbtElement.STRING_TYPE)) fake.putSubTag("author", tag.get("author"));
            if(tag.contains("generation", NbtElement.INT_TYPE)) fake.putSubTag("generation", tag.get("generation"));
            // FIXME: Pages need to be present for the book to work. Force update on selection?
            // Prevents issues with other mods expecting pages to be present.
            fake.putSubTag("pages", new NbtList());
        }

        if(item instanceof WritableBookItem) {
            // FIXME: Pages need to be present for the book to work. Force update on selection?
            // Prevents issues with other mods expecting pages to be present.
            fake.putSubTag("pages", new NbtList());
        }

        return fake;
    }

    /**
     * Minimally copies over the banner NBT based on the provided blockEntity data.
     *
     * Only the NBT required to render a layer is copied over.
     *
     * @param blockEntity The original block entity tag. May be null.
     * @param faked The faked ItemStack to copy to.
     * */
    static void cleanBanner(NbtCompound blockEntity, ItemStack faked) {
        if (blockEntity != null && blockEntity.contains("Patterns", NbtElement.LIST_TYPE)) {
            NbtList fakePatterns = new NbtList();
            int i = 0;
            for(NbtElement pattern : blockEntity.getList("Patterns", NbtElement.COMPOUND_TYPE)) {
                if(!(pattern instanceof NbtCompound)) continue;
                // A 12 layer cap on the rewriting is primarily to prevent
                // the faked stack from becoming the kicking item.
                if(i++ >= 12) break;
                NbtCompound oldPattern = (NbtCompound) pattern;
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
            faked.getOrCreateSubTag("BlockEntityTag").put("Patterns", fakePatterns);
        }
    }
}
