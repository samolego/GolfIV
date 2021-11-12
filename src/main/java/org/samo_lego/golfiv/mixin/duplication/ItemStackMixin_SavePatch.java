package org.samo_lego.golfiv.mixin.duplication;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static org.samo_lego.golfiv.GolfIV.golfConfig;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin_SavePatch {
    @Shadow public abstract void setCount(int count);

    @Shadow public abstract String getTranslationKey();

    @Shadow public abstract Item getItem();

    @Shadow public abstract int getCount();

    @Inject(method = "writeNbt(Lnet/minecraft/nbt/NbtCompound;)Lnet/minecraft/nbt/NbtCompound;", at = @At("HEAD"), cancellable = true)
    private void writeStack(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> cir) {
        if(golfConfig.duplication.patchItemSave) {
            System.out.println("Saving " + this.getTranslationKey());

            ItemStack copy = ((ItemStack) (Object) this).copy();
            this.setCount(0);

            System.out.println("This: " + this.getCount() + " vs copy: " + copy);

            Identifier identifier = Registry.ITEM.getId(copy.getItem());
            nbt.putString("id", identifier.toString());
            nbt.putByte("Count", (byte)copy.getCount());
            if (copy.getNbt() != null) {
                nbt.put("tag", copy.getNbt().copy());
            }

            System.out.println(nbt);

            cir.setReturnValue(nbt);
        }
    }
}
