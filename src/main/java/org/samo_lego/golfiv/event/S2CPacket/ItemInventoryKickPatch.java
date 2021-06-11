package org.samo_lego.golfiv.event.S2CPacket;

import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.samo_lego.golfiv.mixin.accessors.InventoryS2CPacketAccessor;
import org.samo_lego.golfiv.mixin.accessors.ScreenHandlerSlotUpdateS2CPacketAccessor;

import java.util.List;
import java.util.stream.Collectors;

import static org.samo_lego.golfiv.GolfIV.golfConfig;
import static org.samo_lego.golfiv.casts.ItemStackChecker.fakeStack;

public class ItemInventoryKickPatch implements S2CPacketCallback {

    public ItemInventoryKickPatch() {
    }

    /**
     * Changes ItemStacks in {@link InventoryS2CPacket}
     * to not include tags, as they get sent additionally by {@link ScreenHandlerSlotUpdateS2CPacket}
     *
     * @param packet packet being sent
     * @param player player getting the packet
     * @param server Minecraft Server
     */
    @Override
    public void preSendPacket(Packet<?> packet, ServerPlayerEntity player, MinecraftServer server) {
        if(golfConfig.packet.patchItemKickExploit && packet instanceof InventoryS2CPacket) {
            List<ItemStack> contents = ((InventoryS2CPacketAccessor) packet).getContents();
            List<ItemStack> fakedContents = contents.stream().map(stack -> {
                NbtCompound tag = stack.getTag();
                if(tag != null) {
                    // TODO: Perhaps take a more dynamic approach to this?
                    //  This is not really flexible as is and may leave modded items broken.
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
                            NbtCompound blockEntity = stack.getSubTag("BlockEntityTag");
                            if (blockEntity != null && blockEntity.contains("Patterns", NbtElement.LIST_TYPE)) {
                                NbtList fakePatterns = new NbtList();
                                for(NbtElement pattern : blockEntity.getList("Patterns", NbtElement.COMPOUND_TYPE)) {
                                    if(!(pattern instanceof NbtCompound)) continue;
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
                                fake.getOrCreateSubTag("BlockEntityTag").put("Patterns", fakePatterns);
                            }
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

                return stack;
            }).collect(Collectors.toList());

            ((InventoryS2CPacketAccessor) packet).setContents(fakedContents);
        } else if(golfConfig.packet.patchItemKickExploit && packet instanceof ScreenHandlerSlotUpdateS2CPacket) {
            ItemStack stack = ((ScreenHandlerSlotUpdateS2CPacketAccessor) packet).getStack();
            if(stack.getTag() != null) {
                PacketByteBuf testBuf = new PacketByteBuf(Unpooled.buffer());
                if(testBuf.writeItemStack(stack).readableBytes() > 2097140) {
                    ((ScreenHandlerSlotUpdateS2CPacketAccessor) packet).setStack(fakeStack(stack, false));
                }
                testBuf.release();
            }
        }
    }
}
