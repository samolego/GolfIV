package org.samo_lego.golfiv.mixin;


import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerMoveC2SPacket.class)
public interface PlayerMoveC2SPacketAccessor {

    @Accessor("onGround")
    void setOnGround(boolean onGround);

    @Accessor("changePosition")
    boolean changesPosition();

    @Accessor("changeLook")
    boolean changesLook();

    @Accessor("x")
    double x();

    @Accessor("y")
    double y();

    @Accessor("z")
    double z();
}
