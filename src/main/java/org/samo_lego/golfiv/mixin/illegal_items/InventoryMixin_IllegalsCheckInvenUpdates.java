package org.samo_lego.golfiv.mixin.illegal_items;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.collection.DefaultedList;
import org.samo_lego.golfiv.casts.ItemStackChecker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static org.samo_lego.golfiv.GolfIV.golfConfig;

/**
 * Legalizes the entire inventory after certain large inventory updates
 */
@Mixin(PlayerInventory.class)
abstract class InventoryMixin_IllegalsCheckInvenUpdates {
    @Shadow @Final private List<DefaultedList<ItemStack>> combinedInventory;

    @Shadow @Final public PlayerEntity player;

    /**
     * Legalizes the inventory after cloning
     *
     * @param other the inventory that this inventory is cloned from
     * @param ci callback info
     */
    @Inject(method = "clone", at = @At("TAIL"))
    private void onInventoryCopy(PlayerInventory other, CallbackInfo ci) {
        legaliseInventory();
    }

    /**
     * Legalizes the inventory after deserialization
     *
     * @param tag the tag it is deserializing
     * @param ci callback info
     */
    @Inject(method = "readNbt(Lnet/minecraft/nbt/NbtList;)V", at = @At("TAIL"))
    private void onDeserialize(NbtList tag, CallbackInfo ci) {
        legaliseInventory();
    }

    /**
     * Legalizes the entire inventory
     */
    private void legaliseInventory() {
        if(
            (golfConfig.items.survival.legaliseWholeInventory && !this.player.isCreative()) ||
            (golfConfig.items.creative.legaliseWholeInventory && this.player.isCreative())
        )
            for (DefaultedList<ItemStack> stacks : this.combinedInventory) {
                legaliseMany(stacks, !this.player.isCreative());
            }
    }

    /**
     * Legalizes every stack in a DefaultedList of stacks
     *
     * @param stacks the list of stacks
     * @param survival whether or not the inventory holder is in survival
     */
    private void legaliseMany(DefaultedList<ItemStack> stacks, boolean survival) {
        for (ItemStack itemStack : stacks) {
            //noinspection ConstantConditions
            ((ItemStackChecker) (Object) itemStack).makeLegal(survival);
        }
    }
}
