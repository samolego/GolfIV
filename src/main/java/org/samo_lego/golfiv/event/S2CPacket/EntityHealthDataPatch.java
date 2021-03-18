package org.samo_lego.golfiv.event.S2CPacket;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Saddleable;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.samo_lego.golfiv.mixin.accessors.EntityTrackerUpdateS2CPacketAccessor;

import static org.samo_lego.golfiv.GolfIV.golfConfig;

public class EntityHealthDataPatch implements S2CPacketCallback {

    public EntityHealthDataPatch() {
    }
    /**
     * Removes entity health data from packet.
     *
     * @param packet packet being sent
     * @param player player getting the packet
     * @param server Minecraft Server
     */
    @Override
    public void preSendPacket(Packet<?> packet, ServerPlayerEntity player, MinecraftServer server) {
        if(golfConfig.packet.removeHealthTags && packet instanceof EntityTrackerUpdateS2CPacket) {
            EntityTrackerUpdateS2CPacketAccessor p = ((EntityTrackerUpdateS2CPacketAccessor) packet);
            Entity entity = player.getServerWorld().getEntityById(p.getId());

            if(entity instanceof LivingEntity && entity.isAlive() && !(entity instanceof Saddleable)) {
                p.getTrackedValues().removeIf(trackedValue -> trackedValue.getData().getId() == 8);
            }
        }
    }
}
