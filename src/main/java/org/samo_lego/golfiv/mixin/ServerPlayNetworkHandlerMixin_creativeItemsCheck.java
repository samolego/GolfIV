package org.samo_lego.golfiv.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.samo_lego.golfiv.utils.CheatType;
import org.samo_lego.golfiv.utils.casts.Golfer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import static org.samo_lego.golfiv.GolfIV.golfConfig;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin_creativeItemsCheck {
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
            int amount = itemStack.getCount();
            if(itemStack.isStackable()) {
                if(amount > itemStack.getMaxCount())
                    ((Golfer) player).report(CheatType.SUSPICIOUS_CREATIVE);
                amount = itemStack.getCount() <= itemStack.getMaxCount() ? amount : 1;
            }
            else {
                if(amount > 1)
                    ((Golfer) player).report(CheatType.SUSPICIOUS_CREATIVE);
                amount = 1;
            }

            itemStack =  new ItemStack(itemStack.getItem(), amount);
            itemStack.setTag(compoundTag);
        }
        return itemStack;
    }
}
