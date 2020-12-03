package org.samo_lego.golfiv.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.samo_lego.golfiv.casts.ItemStackChecker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.samo_lego.golfiv.GolfIV.golfConfig;

@Mixin(Slot.class)
public abstract class SlotMixin_illegalsCheckSet {

    @Inject(method = "setStack(Lnet/minecraft/item/ItemStack;)V", at = @At("HEAD"))
    private void checkInsertedStack(ItemStack stack, CallbackInfo ci) {
        if(golfConfig.main.checkForStrangeItems)
            //noinspection ConstantConditions
            ((ItemStackChecker) (Object) stack).makeLegal();
    }
}