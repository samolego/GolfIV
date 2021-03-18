package org.samo_lego.golfiv.mixin_checks.illegals;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import static org.samo_lego.golfiv.GolfIV.golfConfig;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin_creativeItemsCheck {
    @Shadow public ServerPlayerEntity player;

    /**
     * Clears the CompoundTags from creative items while still allowing pick block function
     *
     * @param itemStack
     * @return
     */
    @ModifyVariable(
            method = "onCreativeInventoryAction(Lnet/minecraft/network/packet/c2s/play/CreativeInventoryActionC2SPacket;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;getSubTag(Ljava/lang/String;)Lnet/minecraft/nbt/CompoundTag;"
            )
    )
    private ItemStack checkCreativeItem(ItemStack itemStack) {
        if(golfConfig.main.preventCreativeStrangeItems) {
            CompoundTag compoundTag = itemStack.getSubTag("BlockEntityTag");
            int amount = itemStack.getCount();
            if(amount > itemStack.getMaxCount()) {
                amount = 1;
            }
            itemStack =  new ItemStack(itemStack.getItem(), amount);
            itemStack.setTag(compoundTag);
        }
        return itemStack;
    }
}