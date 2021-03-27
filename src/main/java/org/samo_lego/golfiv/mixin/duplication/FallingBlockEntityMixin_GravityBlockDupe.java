package org.samo_lego.golfiv.mixin.duplication;

import net.minecraft.entity.FallingBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.samo_lego.golfiv.GolfIV.golfConfig;

/**
 * Fixes gravity block duping.
 */
@Mixin(FallingBlockEntity.class)
public class FallingBlockEntityMixin_GravityBlockDupe {

    /**
     * Before the block is placed, we check if it has been removed.
     * If so, we don't place it.
     * @param ci callback info (mixin)
     */
    @Inject(
            method = "tick()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"
            ),
            cancellable = true
    )
    private void midTick(CallbackInfo ci) {
        if(golfConfig.duplication.patchGravityBlock && ((FallingBlockEntity) (Object) this).removed)
            ci.cancel();
    }

}
