package org.samo_lego.golfiv.mixin;


import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.samo_lego.golfiv.GolfIV.golfConfig;
import static org.samo_lego.golfiv.utils.BallLogger.logInfo;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {

    private final PlayerEntity player = (PlayerEntity) (Object) this;

    @Inject(
            method = "attack(Lnet/minecraft/entity/Entity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;handleAttack(Lnet/minecraft/entity/Entity;)Z",
                    shift = At.Shift.AFTER
            ),
            cancellable = true)
    private void checkAttackDistance(Entity target, CallbackInfo ci) {
        if(player.distanceTo(target) > (player.isCreative() ? 5 : 3)) {
            if(golfConfig.logging.toConsole)
                logInfo("Player " + player.getGameProfile().getName() + " might be using reach hacks!(PE)");
            ci.cancel();
        }
    }
}
