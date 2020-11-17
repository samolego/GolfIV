package org.samo_lego.golfiv.mixin;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import static org.samo_lego.golfiv.GolfIV.golfConfig;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Shadow public ServerPlayerEntity player;

    @ModifyVariable(
            method = "onCreativeInventoryAction(Lnet/minecraft/network/packet/c2s/play/CreativeInventoryActionC2SPacket;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;getSubTag(Ljava/lang/String;)Lnet/minecraft/nbt/CompoundTag;"
            )
    )
    private ItemStack checkCreativeItem(ItemStack itemStack) {
        if(golfConfig.main.preventStrangeCreativeItems) {
            CompoundTag compoundTag = itemStack.getSubTag("BlockEntityTag");
            int amount;
            if(itemStack.isStackable()) {
                amount = itemStack.getCount() <= itemStack.getMaxCount() ? itemStack.getCount() : 1;
            }
            else
                amount = 1;

            itemStack =  new ItemStack(itemStack.getItem(), amount);
            itemStack.setTag(compoundTag);
        }
        return itemStack;
    }
}
