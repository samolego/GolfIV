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
import static org.samo_lego.golfiv.utils.CheatType.HIT_THROUGH_WALLS;
import static org.samo_lego.golfiv.utils.CheatType.REACH;

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
        if(golfConfig.main.hitCheck) {
            EntityHitResult entityHit = new EntityHitResult(victim);

            if(!player.isCreative() && entityHit.squaredDistanceTo(player) > 16) {
                ((Golfer) player).report(REACH, 5);
                ci.cancel();
            }

            BlockHitResult blockHit = (BlockHitResult) player.raycast(Math.sqrt(distanceSquared), 0, false);
            BlockState blockState = serverWorld.getBlockState(blockHit.getBlockPos());

            if(blockState.isFullCube(serverWorld, blockHit.getBlockPos()) && blockHit.squaredDistanceTo(player) + 1.0D < entityHit.squaredDistanceTo(player)) {
                ((Golfer) player).report(HIT_THROUGH_WALLS, 10);
                ci.cancel();
            }
        }
    }
}
