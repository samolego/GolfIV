package org.samo_lego.golfiv.mixin_checks.illegalActions;

import net.minecraft.entity.player.PlayerEntity;
import org.samo_lego.golfiv.casts.Golfer;
import org.samo_lego.golfiv.mixin_checks.accessors.EntityAccessor;
import org.samo_lego.golfiv.utils.CheatType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.samo_lego.golfiv.GolfIV.golfConfig;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin_PortalsGuiCheck {
    @Shadow
    protected abstract void closeHandledScreen();

    private final Golfer golfer = (Golfer) this;

    /**
     * Increments portal tick if gui open and
     * player in portal, resets if it closes
     * or the player leaves the portal
     */
    @Inject(method = "tick()V", at = @At("TAIL"))
    private void portalTick(CallbackInfo ci) {
        if (this.golfer.hasOpenGui() && ((EntityAccessor) this).inNetherPortal()) {
            this.golfer.setGuiOpenInPortalTicks(this.golfer.getGuiOpenInPortalTicks() + 1);
        } else if (this.golfer.getGuiOpenInPortalTicks() != 0) {
            this.golfer.setGuiOpenInPortalTicks(0);
        }
        if (this.golfer.getGuiOpenInPortalTicks() > 9) {
            this.golfer.report(CheatType.PORTAL_HACK, golfConfig.sus.portalHack);
            this.closeHandledScreen();
            this.golfer.setOpenGui(false);
        }
    }
}