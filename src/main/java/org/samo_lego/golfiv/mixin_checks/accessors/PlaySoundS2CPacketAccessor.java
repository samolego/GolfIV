package org.samo_lego.golfiv.mixin_checks.accessors;

import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket.class)
public interface PlaySoundS2CPacketAccessor {
    @Accessor("fixedX")
    int getX();

    @Accessor("fixedZ")
    int getZ();

    @Accessor("fixedX")
    void setX(int x);

    @Accessor("fixedZ")
    void setZ(int z);

    @Accessor("sound")
    SoundEvent getSound();
}
