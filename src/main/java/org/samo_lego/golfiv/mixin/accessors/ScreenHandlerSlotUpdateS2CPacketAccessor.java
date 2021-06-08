package org.samo_lego.golfiv.mixin.accessors;

import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ScreenHandlerSlotUpdateS2CPacket.class)
public interface ScreenHandlerSlotUpdateS2CPacketAccessor {
    @Accessor("stack")
    ItemStack getStack();

    @Mutable
    @Accessor("stack")
    void setStack(ItemStack fakeStack);
}
