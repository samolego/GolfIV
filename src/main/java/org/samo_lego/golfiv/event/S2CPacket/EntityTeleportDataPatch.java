package org.samo_lego.golfiv.event.S2CPacket;

import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.samo_lego.golfiv.mixin.accessors.EntityPositionS2CPacketAccessor;

import static org.samo_lego.golfiv.GolfIV.golfConfig;

public class EntityTeleportDataPatch implements S2CPacketCallback {
    public EntityTeleportDataPatch() {
    }

    /**
     * If player teleports out of render distance, we modify the coordinates of the
     * packet, in order to hide player's original TP coordinates.
     *
     * @param packet packet being sent
     * @param player player getting the packet
     * @param server Minecraft Server
     */

    @Override
    public void preSendPacket(Packet<?> packet, ServerPlayerEntity player, MinecraftServer server) {
        if(golfConfig.packet.removeTeleportData && packet instanceof EntityPositionS2CPacket) {
            EntityPositionS2CPacketAccessor packetAccessor = (EntityPositionS2CPacketAccessor) packet;

            // Similar to SoundExploitPatch#preSendPacket
            int maxPlayerDistance = server.getPlayerManager().getViewDistance() * 16;
            double deltaX = player.getX() - packetAccessor.getX();
            double deltaZ = player.getZ() - packetAccessor.getZ();

            double actualPlayerDistance = deltaX * deltaZ;

            if(maxPlayerDistance * maxPlayerDistance < Math.abs(actualPlayerDistance)) {
                // Can not track this entity (teleporter), why send data?
                packetAccessor.setX(player.getX() + maxPlayerDistance);
                packetAccessor.setZ(player.getZ() + maxPlayerDistance);
            }
        }
    }
}
