package org.samo_lego.golfiv.mixin.combat;

import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static org.samo_lego.golfiv.GolfIV.golfConfig;

/**
 * Checks for hitting through walls.
 */
@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin_HitCheck {

    @Shadow public ServerPlayerEntity player;

    /**
     * Checks whether player is hitting entity through wall
     * by comparing raycast distance of the block and targeted entity.
     *
     * Checks distance from attacker to attacked entity as well,
     * in order to prevent reach hacks.
     *
     * Also checks the angle at which player is hitting the entity.
     *
     * @param packet
     * @param ci
     * @param serverWorld
     * @param victim
     * @param distanceSquared
     */
    @Inject(
            method = "onPlayerInteractEntity(Lnet/minecraft/network/packet/c2s/play/PlayerInteractEntityC2SPacket;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/packet/c2s/play/PlayerInteractEntityC2SPacket;getHand()Lnet/minecraft/util/Hand;"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true
    )
    private void hitThroughWallCheck(PlayerInteractEntityC2SPacket packet, CallbackInfo ci, ServerWorld serverWorld, Entity victim, double distanceSquared) {
        EntityHitResult entityHit = new EntityHitResult(victim);
        double victimDistanceSquared = entityHit.squaredDistanceTo(player);
        double victimDistance = Math.sqrt(victimDistanceSquared);

        if(golfConfig.combat.checkHitDistance && !player.isCreative() && victimDistanceSquared > 22) {
            ci.cancel();
            return;
        }
        if(golfConfig.combat.checkWallHit) {
            // Through-wall hit check
            BlockHitResult blockHit = (BlockHitResult) player.raycast(Math.sqrt(distanceSquared), 0, false);

            if(Math.sqrt(blockHit.squaredDistanceTo(player)) + 0.5D < victimDistance) {
                ci.cancel();
                return;
            }
        }
        if(golfConfig.combat.checkHitAngle) {
            // Angle check
            int xOffset = player.getHorizontalFacing().getOffsetX();
            int zOffset = player.getHorizontalFacing().getOffsetZ();
            Box bBox = victim.getBoundingBox();

            // Checking if victim is behind player
            if(xOffset * victim.getX() + bBox.getXLength() / 2 - xOffset * player.getX() < 0 || zOffset * victim.getZ() + bBox.getZLength() / 2 - zOffset * player.getZ() < 0) {
                // "Dumb" check
                ci.cancel();
                return;
            }
            double deltaX = victim.getX() - player.getX();
            double deltaZ = victim.getZ() - player.getZ();
            double beta = Math.atan2(deltaZ, deltaX) - Math.PI / 2;

            double phi = beta - Math.toRadians(player.yaw);
            //todo can be improved?
            double allowedAttackSpace = Math.sqrt(bBox.getXLength() * bBox.getXLength() + bBox.getZLength() * bBox.getZLength());

            if(Math.abs(victimDistance * Math.sin(phi)) > allowedAttackSpace / 2 + 0.2D) {
                // Fine check
                ci.cancel();
            }
        }
    }
}
