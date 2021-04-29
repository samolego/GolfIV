package org.samo_lego.golfiv.mixin.accessors;

import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityPositionS2CPacket.class)
public interface EntityPositionS2CPacketAccessor {
    @Accessor("x")
    double getX();

    @Accessor("z")
    double getZ();

    @Accessor("x")
    void setX(double X);

    @Accessor("z")
    void setZ(double z);
}
