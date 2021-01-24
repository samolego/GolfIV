package org.samo_lego.golfiv.mixin_checks.movement;

import net.minecraft.item.FireworkItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
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
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static org.samo_lego.golfiv.GolfIV.golfConfig;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin_ElytraFlightCheck {

    @Shadow public ServerPlayerEntity player;
    @Unique
    private final NetworkHandlerData data = (NetworkHandlerData) this;
    @Unique
    private boolean usedRocket;


    /**
     * Checks elytra movement and compares it to expected movement, found in FireworkRocketEntity#tick
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
    private void checkElytraMovement(PlayerMoveC2SPacket packet, CallbackInfo ci) {
        if(golfConfig.movement.checkElytraFlight && player.isFallFlying() && data.getLastMovement() != null && data.getPacketMovement().getY() > data.getLastMovement().getY() && !player.isUsingRiptide()) {
            if(usedRocket) {
                usedRocket = false;
            }
            else if(data.getPacketMovement().lengthSquared() > data.getLastMovement().lengthSquared()) {
                Vec3d rotation = this.player.getRotationVector();
                Vec3d lastMovement = data.getLastMovement();
                lastMovement = lastMovement.add(rotation.x * 0.1D + (rotation.x * 1.5D - lastMovement.x) * 0.5D, rotation.y * 0.1D + (rotation.y * 1.5D - lastMovement.y) * 0.5D, rotation.z * 0.1D + (rotation.z * 1.5D - lastMovement.z) * 0.5D);

                if(data.getPacketMovement().lengthSquared() - lastMovement.lengthSquared() > 0.067D) {
                    ((Golfer) this.player).report(CheatType.ELYTRA_FLIGHT, golfConfig.sus.elytraFlight);
                    this.player.requestTeleport(player.getX(), player.getY(), player.getZ());
                    ci.cancel();
                }
            }
        }
    }

    /**
     * Detects firework boosting.
     *
     * @param packet
     * @param ci
     * @param serverWorld
     * @param hand
     * @param itemStack
     * @param actionResult
     */
    @Inject(
            method = "onPlayerInteractItem(Lnet/minecraft/network/packet/c2s/play/PlayerInteractItemC2SPacket;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/ActionResult;shouldSwingHand()Z"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void checkFireworkUse(PlayerInteractItemC2SPacket packet, CallbackInfo ci, ServerWorld serverWorld, Hand hand, ItemStack itemStack, ActionResult actionResult) {
        if(actionResult.equals(ActionResult.CONSUME) && itemStack.getItem() instanceof FireworkItem && player.isFallFlying()) {
            this.usedRocket = true;
        }
    }
}
