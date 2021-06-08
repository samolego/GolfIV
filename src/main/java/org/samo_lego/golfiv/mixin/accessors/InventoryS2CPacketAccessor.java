package org.samo_lego.golfiv.mixin.accessors;

import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(InventoryS2CPacket.class)
public interface InventoryS2CPacketAccessor {
    @Accessor("contents")
    List<ItemStack> getContents();

    @Mutable
    @Accessor("contents")
    void setContents(List<ItemStack> fakedContents);
}
