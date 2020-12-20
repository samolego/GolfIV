package org.samo_lego.golfiv.mixin_checks.combat;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import org.samo_lego.golfiv.casts.Golfer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static org.samo_lego.golfiv.GolfIV.golfConfig;
import static org.samo_lego.golfiv.utils.CheatType.*;

/**
 * Checks for hitting through walls.
 */
@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin_hitCheck {

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
        double dist2 = entityHit.squaredDistanceTo(player);

        if(golfConfig.combat.checkHitDistance && !player.isCreative() && dist2 > 22) {
            System.out.println(dist2);
            ((Golfer) player).report(REACH, 22);
            ci.cancel();
            return;
        }
        if(golfConfig.combat.checkHitAngle) {
            // Angle check
            float yaw = player.yaw;
            int xOffset = player.getHorizontalFacing().getOffsetX();
            int zOffset = player.getHorizontalFacing().getOffsetZ();

            // Checking if victim is behind player
            if(xOffset * victim.getX() - xOffset * player.getX() < 0 || zOffset * victim.getZ() - zOffset * player.getZ() < 0) {
                // "Dumb" check
                ((Golfer) player).report(KILLAURA, 20);
                ci.cancel();
                return;
            }
            double deltaX = victim.getX() - player.getX();
            double deltaZ = victim.getZ() - player.getZ();
            double beta = Math.atan2(deltaZ, deltaX) - Math.PI / 2;

            double phi = beta - Math.toRadians(yaw);
            if(Math.abs(Math.sqrt(dist2) * Math.sin(phi)) > 0.7D){
                // Fine check
                ((Golfer) player).report(KILLAURA, 5);
                ci.cancel();
                return;
            }
        }
        if(golfConfig.combat.checkWallHit) {
            // Through-wall hit check
            BlockHitResult blockHit = (BlockHitResult) player.raycast(Math.sqrt(distanceSquared), 0, false);
            BlockState blockState = serverWorld.getBlockState(blockHit.getBlockPos());

            if(blockHit.squaredDistanceTo(player) + 1.0D < dist2) {
                ((Golfer) player).report(HIT_THROUGH_WALLS, 10);
                ci.cancel();
            }
        }
    }
}
