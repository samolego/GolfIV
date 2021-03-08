package org.samo_lego.golfiv.mixin_checks.movement;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.samo_lego.golfiv.casts.Golfer;
import org.samo_lego.golfiv.casts.NetworkHandlerData;
import org.samo_lego.golfiv.utils.CheatType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.samo_lego.golfiv.GolfIV.golfConfig;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin_SpeedCheck {

    @Shadow public ServerPlayerEntity player;
    @Unique
    private final NetworkHandlerData data = (NetworkHandlerData) this;

    /**
     * Checks the Y difference when in air in order
     * to detect speed hacks.
     *
     * @param packet
     * @param ci
     */
    @Inject(
            method = "onPlayerMove(Lnet/minecraft/network/packet/c2s/play/PlayerMoveC2SPacket;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;hasVehicle()Z"
            ),
            cancellable = true
    )
    private void checkAirMovement(PlayerMoveC2SPacket packet, CallbackInfo ci) {
        if(golfConfig.movement.noSpeed && !player.isFallFlying() && !player.isCreative() && !player.isSpectator() && data.getLastMovement() != null && !((Golfer) player).isNearFluid()) {
            Vec3d packetMovement = data.getPacketMovement();

            double predictedDist = data.getLastDist() * 0.91F;
            double packetDist = packetMovement.x * packetMovement.x + packetMovement.z * packetMovement.z;
            double distDelta = packetDist - predictedDist;

            if(!data.wasLLastOnGround() && !data.wasLastOnGround() && !((Golfer) player).isNearGround() && distDelta > 0.00750716D) {
                ((Golfer) this.player).report(CheatType.SPEED_HACK, golfConfig.sus.speedHack);
                this.player.requestTeleport(player.getX(), player.getY(), player.getZ());
                ci.cancel();
            }
            data.setLastDist(packetDist);
        }
    }
}
