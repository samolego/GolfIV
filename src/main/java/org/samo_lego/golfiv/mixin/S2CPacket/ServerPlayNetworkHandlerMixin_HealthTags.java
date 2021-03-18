package org.samo_lego.golfiv.mixin.S2CPacket;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Saddleable;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.samo_lego.golfiv.mixin.accessors.EntityTrackerUpdateS2CPacketAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.samo_lego.golfiv.GolfIV.golfConfig;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin_HealthTags {
    @Shadow public ServerPlayerEntity player;

    /**
     * Removes entity health data from packet.
     *
     * @param packet
     * @param ci
     */
    @Inject(
            method = "sendPacket(Lnet/minecraft/network/Packet;)V",
            at = @At("HEAD")
    )
    private void removeHealthTags(Packet<?> packet, CallbackInfo ci) {
        if(golfConfig.packet.removeHealthTags && packet instanceof EntityTrackerUpdateS2CPacket) {

            EntityTrackerUpdateS2CPacketAccessor p = ((EntityTrackerUpdateS2CPacketAccessor) packet);
            Entity entity = this.player.getServerWorld().getEntityById(p.getId());

            if(entity instanceof LivingEntity && entity.isAlive() && !(entity instanceof Saddleable)) {
                p.getTrackedValues().removeIf(trackedValue -> trackedValue.getData().getId() == 8);
            }
        }
    }
}
