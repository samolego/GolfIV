package org.samo_lego.golfiv.mixin_checks.movement;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
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
        if(!data.wasLLastOnGround() && !data.wasLastOnGround() && !((Golfer) player).isNearGround() && !player.isClimbing() && !player.isFallFlying() && !((LivingEntityAccessor) player).jumping() && data.getLastMovement() != null) {
            Entity vehicle = player.getRootVehicle();
            if(vehicle == null) {
                vehicle = player;
            }

            if(golfConfig.main.noFly && !this.player.abilities.allowFlying) {
                double d = 0.08D;
                boolean falling = data.getPacketMovement().getY() <= 0.0D;
                boolean wasFalling = data.getLastMovement().getY() <= 0.0D;

                if(vehicle.isInLava()) {
                    //todo
                }
                else if(vehicle.isTouchingWater()) {
                    //todo
                }
                else {
                    // LivingEntity#travel
                    if(wasFalling && falling && vehicle instanceof LivingEntity && ((LivingEntity) vehicle).hasStatusEffect(StatusEffects.SLOW_FALLING)) {
                        d = 0.01D;
                    }

                    double predictedDeltaY;

                    //System.out.println("top reached: " + !(!wasFalling && falling));
                    if (vehicle instanceof LivingEntity && ((LivingEntity) vehicle).hasStatusEffect(StatusEffects.LEVITATION))
                        predictedDeltaY = data.getLastMovement().getY() + (0.05D * (double)(((LivingEntity) vehicle).getStatusEffect(StatusEffects.LEVITATION).getAmplifier() + 1) - data.getLastMovement().getY()) * 0.2D;
                    else
                        predictedDeltaY = data.getLastMovement().y - d;

                    predictedDeltaY *= 0.9800000190734863D;

                    //System.out.println(Math.abs(predictedDeltaY - data.getPacketMovement().getY()));
                    if(Math.abs(predictedDeltaY) >= 0.005D && Math.abs(predictedDeltaY - data.getPacketMovement().getY()) > 0.003D) {
                        if(++this.flyCounter > 1)
                            ((Golfer) this.player).report(FLY_HACK, 20);
                    }
                    else
                        this.flyCounter += this.flyCounter > 0 ? -1 : 0;
                }
            }
        }
    }
}
