package org.samo_lego.golfiv.mixin_checks.combat;

import net.minecraft.entity.LivingEntity;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.samo_lego.golfiv.mixin_checks.accessors.EntityTrackerUpdateS2CPacketAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.samo_lego.golfiv.GolfIV.golfConfig;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin_HealthTags {
    @Shadow public ServerPlayerEntity player;

    @Inject(
            method = "sendPacket(Lnet/minecraft/network/Packet;)V",
            at = @At("HEAD")
    )
    private void removeHealthTags(Packet<?> packet, CallbackInfo ci) {
        if(golfConfig.entityDataPacket.removeHealthTags && packet instanceof EntityTrackerUpdateS2CPacket) {
            EntityTrackerUpdateS2CPacketAccessor p = ((EntityTrackerUpdateS2CPacketAccessor) packet);
            int entityId = p.getID();
            if(this.player.getServerWorld().getEntityById(entityId) instanceof LivingEntity) {
                p.getTrackedValues().removeIf(trackedValue -> trackedValue.getData().getId() == 8);
            }
        }
    }
}
