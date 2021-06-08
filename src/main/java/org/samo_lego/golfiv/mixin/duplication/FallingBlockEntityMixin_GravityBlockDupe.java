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
     * If block has been removed previous tick (or during the tick),
     * cancel the method.
     * It will be removed anyways as seen in target mixin value.
     */
    @Inject(
            method = "tick()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/FallingBlockEntity;discard()V"
            ),
            cancellable = true
    )
    private void midTick(CallbackInfo ci) {
        if(golfConfig.duplication.patchGravityBlock && ((FallingBlockEntity) (Object) this).isRemoved())
            ci.cancel();
    }

}
