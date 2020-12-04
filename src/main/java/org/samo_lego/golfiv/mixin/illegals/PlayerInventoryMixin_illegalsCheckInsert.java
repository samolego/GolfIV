package org.samo_lego.golfiv.mixin.illegals;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.samo_lego.golfiv.casts.Golfer;
import org.samo_lego.golfiv.casts.ItemStackChecker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import static org.samo_lego.golfiv.GolfIV.golfConfig;
import static org.samo_lego.golfiv.utils.CheatType.NBT_ITEMS;

@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin_illegalsCheckInsert {

    @Shadow @Final public PlayerEntity player;

    /**
     * Checks if the insrted stack to
     * player's inventory was legal.
     * Catches /give as well.
     *
     * @param stack
     * @return
     */
    @ModifyVariable(method = "insertStack(ILnet/minecraft/item/ItemStack;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isDamaged()Z"))
    private ItemStack checkInsertedStack(ItemStack stack) {
        //noinspection ConstantConditions
        boolean illegal = ((ItemStackChecker) (Object) stack).makeLegal();
        if(golfConfig.main.checkForStrangeItems && illegal) {
            ((Golfer) player).report(NBT_ITEMS);
        }
        return stack;
    }
}
