package org.samo_lego.golfiv.mixin.duplication;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static org.samo_lego.golfiv.GolfIV.golfConfig;

/**
 * Checks whether player's connection is open
 * before applying damage. Prevents duplication of items
 * when player disconnects and dies right after data is saved.
 *
 * Thanks to @SpaceClouds42 for bringing this up :)
 */
@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin_NoConnectionDeath {

    @Shadow public abstract boolean isDisconnected();

    /**
     * Checks whether player is even connected before applying damage.
     */
    @Inject(method = "damage(Lnet/minecraft/entity/damage/DamageSource;F)Z", at = @At("HEAD"), cancellable = true)
    private void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if(golfConfig.duplication.patchDeathDuplication && this.isDisconnected())
            cir.setReturnValue(false);
    }
}
