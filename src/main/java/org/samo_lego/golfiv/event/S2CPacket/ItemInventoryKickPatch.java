package org.samo_lego.golfiv.event.S2CPacket;

import io.netty.buffer.Unpooled;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
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
                CompoundTag tag = stack.getTag();
                if(tag != null) {
                    stack = fakeStack(stack, false);
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
            }
        }
    }
}
