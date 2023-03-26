package org.samo_lego.golfiv.event.S2CPacket;

import io.netty.buffer.Unpooled;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.samo_lego.golfiv.casts.ItemStackChecker;
import org.samo_lego.golfiv.mixin.accessors.InventoryS2CPacketAccessor;
import org.samo_lego.golfiv.mixin.accessors.ScreenHandlerSlotUpdateS2CPacketAccessor;

import java.util.List;
import java.util.function.Consumer;

import static org.samo_lego.golfiv.GolfIV.golfConfig;
import static org.samo_lego.golfiv.casts.ItemStackChecker.creativeInventoryStack;
import static org.samo_lego.golfiv.casts.ItemStackChecker.inventoryStack;

public class ItemInventoryKickPatch implements S2CPacketCallback {

    public ItemInventoryKickPatch() {
    }

    /**
     * Removes non-critical tags from item stacks in the event of a
     * creative player and packet overflows.
     * <p>
     * Creative players will additionally also get a {@code GolfIV} tag
     * injected as a hash of the original NBT to prevent Creative inventory
     * management from unexpectedly mangling the item either by just opening
     * the inventory or by {@link org.samo_lego.golfiv.storage.GolfConfig.IllegalItems.Creative#removeCreativeNBTTags
     * Remove Creative NBT Tags}.
     *
     * @param packet packet being sent
     * @param player player getting the packet
     * @param server Minecraft Server
     * @see ItemStackChecker#fakeStack(ItemStack, boolean)
     * @see ItemStackChecker#inventoryStack(ItemStack)
     * @see org.samo_lego.golfiv.mixin.illegal_items.ServerPlayNetworkHandlerMixin_CreativeItemsCheck
     */
    @Override
    public void preSendPacket(Packet<?> packet, ServerPlayerEntity player, MinecraftServer server) {
        if (!golfConfig.packet.patchItemKickExploit) return;
        if (packet instanceof InventoryS2CPacket inventoryPacket) {
            // The creative player does some weirdness in regards to sending packets.
            // This will try to guarantee that the creative player will not accidentally
            // wipe their inventory of various NBT required for the item.

            // For the moment, this may produce some weirdness in regards to rendering as this doesn't quite
            // send all possible NBT. However, the inventory will remain intact, which arguably is better
            // than some missing data for the client.
            if (player.isCreative() || isOversized(packet::write)) {
                List<ItemStack> contents = inventoryPacket.getContents();
                List<ItemStack> fakedContents = contents.stream().map(player.isCreative() ?
                                ItemStackChecker::creativeInventoryStack :
                                ItemStackChecker::inventoryStack)
                        .toList();

                ((InventoryS2CPacketAccessor) packet).setContents(fakedContents);
            }
        } else if (packet instanceof ScreenHandlerSlotUpdateS2CPacket) {
            ItemStack stack = ((ScreenHandlerSlotUpdateS2CPacketAccessor) packet).getStack();
            if (stack.hasNbt() && (player.isCreative() || isOversized(buf -> buf.writeItemStack(stack)))) {
                ((ScreenHandlerSlotUpdateS2CPacketAccessor) packet).setStack(player.isCreative() ? creativeInventoryStack(stack) : inventoryStack(stack));
            }
        }
    }

    /**
     * Tests if the packet will overflow the maximum allowed for the buffer.
     *
     * @param packet The packet in the form of a consumer. Allows for arbitrary packets.
     * @return true if the packet is greater than 2,097,140, false otherwise.
     */
    private static boolean isOversized(Consumer<PacketByteBuf> packet) {
        PacketByteBuf testBuf = new PacketByteBuf(Unpooled.buffer());
        try {
            packet.accept(testBuf);
            return testBuf.readableBytes() > 2097140;
        } finally {
            testBuf.release();
        }
    }
}
