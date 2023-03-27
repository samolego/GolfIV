package org.samo_lego.golfiv.event.S2CPacket;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public interface S2CPacketCallback {

    Event<S2CPacketCallback> EVENT = EventFactory.createArrayBacked(S2CPacketCallback.class,
        (listeners) -> (packet, player, server) -> {
            for (S2CPacketCallback listener : listeners) {
                listener.preSendPacket(packet, player, server);
            }
     });

    /**
     * Provides option to modify outgoing packets.
     *
     * @param packet packet being sent
     * @param player player getting the packet
     * @param server Minecraft Server
     */
    void preSendPacket(Packet<?> packet, ServerPlayerEntity player, MinecraftServer server);
}
