package org.samo_lego.golfiv.mixin.illegal_items;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.samo_lego.golfiv.casts.ItemStackChecker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import static org.samo_lego.golfiv.GolfIV.golfConfig;

/**
 * Checks if the inserted stack is illegal.
 */
@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin_IllegalsCheckInsert {

    @Shadow @Final public PlayerEntity player;

    /**
     * Checks if the inserted stack to
     * player's inventory was legal.
     * Catches /give as well.
     *
     * @param stack item stack to be checked
     * @return "legalised" item stack
     */
    @ModifyVariable(method = "insertStack(ILnet/minecraft/item/ItemStack;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isDamaged()Z"))
    private ItemStack checkInsertedStack(ItemStack stack) {
        if(golfConfig.items.legaliseSurvivalItems) {
            //noinspection ConstantConditions
            ((ItemStackChecker) (Object) stack).makeLegal();
        }
        return stack;
    }
}
