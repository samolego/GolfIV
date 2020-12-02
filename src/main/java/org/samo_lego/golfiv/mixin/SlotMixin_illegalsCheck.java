package org.samo_lego.golfiv.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import static org.samo_lego.golfiv.GolfIV.golfConfig;

@Mixin(Slot.class)
public class SlotMixin_illegalsCheck {

    @ModifyVariable(method = "setStack(Lnet/minecraft/item/ItemStack;)V", at = @At("HEAD"))
    private ItemStack checkInsertedStack(ItemStack stack) {
        return golfConfig.main.checkForStrangeItems ? checkItemStack(stack) : stack;
    }


    @Unique
    private ItemStack checkItemStack(ItemStack itemStack) {
        CompoundTag tag = itemStack.getTag();
        if(tag != null) {
            if(!itemStack.isEnchantable()) {
                tag.remove("Enchantments");
            }
            else {
                for(Tag ench : itemStack.getEnchantments()) {
                    if(((CompoundTag) ench).getInt("lvl") > 5) {
                        tag.remove("Enchantments");
                        break;
                    }
                }
            }
        }

        int amount = itemStack.getCount();
        if(itemStack.isStackable()) {
            amount = amount <= itemStack.getMaxCount() ? amount : 1;
        }
        else {
            amount = 1;
        }
        itemStack.setCount(amount);

        return itemStack;
    }
}
