package org.samo_lego.golfiv.mixin_checks.movement;

import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.samo_lego.golfiv.casts.Golfer;
import org.samo_lego.golfiv.casts.NetworkHandlerData;
import org.samo_lego.golfiv.mixin_checks.accessors.LivingEntityAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.samo_lego.golfiv.GolfIV.golfConfig;
import static org.samo_lego.golfiv.utils.CheatType.FLY_HACK;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin_FlightCheck {
    @Shadow
    public ServerPlayerEntity player;
    @Unique
    private final NetworkHandlerData data = (NetworkHandlerData) this;
    @Unique
    private byte flyCounter;

    @Inject(
            method = "onPlayerMove(Lnet/minecraft/network/packet/c2s/play/PlayerMoveC2SPacket;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;hasVehicle()Z"
            )
    )
    private void checkFlight(PlayerMoveC2SPacket packet, CallbackInfo ci) {
        if(
                golfConfig.movement.checkFlight &&
                !this.player.abilities.allowFlying &&
                !((Golfer) player).isNearFluid() &&
                !data.wasLLastOnGround() &&
                !data.wasLastOnGround() &&
                !((Golfer) player).isNearGround() &&
                !player.isClimbing() &&
                !player.isFallFlying() &&
                !((LivingEntityAccessor) player).jumping() &&
                !player.hasVehicle() &&
                data.getLastMovement() != null &&
                (player.fallDistance > 0.2F || player.fallDistance == 0.0F) // Honey block FP fix
        ) {
            double d = 0.08D;
            double predictedDeltaY;
            boolean falling = data.getPacketMovement().getY() <= 0.0D;
            boolean wasFalling = data.getLastMovement().getY() <= 0.0D;

            // LivingEntity#travel
            if(wasFalling && falling && player.hasStatusEffect(StatusEffects.SLOW_FALLING)) {
                d = 0.01D;
            }

            //System.out.println("top reached: " + !(!wasFalling && falling));
            if (player.hasStatusEffect(StatusEffects.LEVITATION))
                predictedDeltaY = data.getLastMovement().getY() + (0.05D * (double) player.getStatusEffect(StatusEffects.LEVITATION).getAmplifier() + 1) - data.getLastMovement().getY() * 0.2D;
            else
                predictedDeltaY = data.getLastMovement().y - d;

            predictedDeltaY *= 0.9800000190734863D;

            //System.out.println(Math.abs(predictedDeltaY - data.getPacketMovement().getY()));
            if(Math.abs(predictedDeltaY) >= 0.005D && Math.abs(predictedDeltaY - data.getPacketMovement().getY()) > 0.003D) {
                if(++this.flyCounter > 4)
                    ((Golfer) this.player).report(FLY_HACK, golfConfig.sus.flyHack);
            }
            else {
                this.flyCounter += this.flyCounter > 0 ? -1 : 0;
            }
        }
    }
}
