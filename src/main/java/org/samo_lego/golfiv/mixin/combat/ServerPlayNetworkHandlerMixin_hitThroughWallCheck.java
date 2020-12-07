package org.samo_lego.golfiv.mixin.combat;

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

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin_hitThroughWallCheck {

    @Shadow public ServerPlayerEntity player;

    @Inject(
            method = "onPlayerInteractEntity(Lnet/minecraft/network/packet/c2s/play/PlayerInteractEntityC2SPacket;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/packet/c2s/play/PlayerInteractEntityC2SPacket;getHand()Lnet/minecraft/util/Hand;"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true
    )
    private void checkKillaura(PlayerInteractEntityC2SPacket packet, CallbackInfo ci, ServerWorld serverWorld, Entity victim, double distanceSquared) {
        if(golfConfig.main.hitThroughWallCheck) {
            EntityHitResult entityHit = new EntityHitResult(victim);

            BlockHitResult blockHit = (BlockHitResult) player.raycast(Math.sqrt(distanceSquared), 0, false);
            BlockState blockState = serverWorld.getBlockState(blockHit.getBlockPos());

            if(blockState.isFullCube(serverWorld, blockHit.getBlockPos()) && blockHit.squaredDistanceTo(player) < entityHit.squaredDistanceTo(player)) {
                ((Golfer) player).report(HIT_THROUGH_WALLS);
                ci.cancel();
            }
        }
    }
}
