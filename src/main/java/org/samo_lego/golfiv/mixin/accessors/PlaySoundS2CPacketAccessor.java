package org.samo_lego.golfiv.mixin.accessors;

import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlaySoundS2CPacket.class)
public interface PlaySoundS2CPacketAccessor {
    @Accessor("fixedX")
    int getX();

    @Accessor("fixedZ")
    int getZ();

    @Mutable
    @Accessor("fixedX")
    void setX(int x);

    @Mutable
    @Accessor("fixedZ")
    void setZ(int z);

    @Accessor("sound")
    RegistryEntry<SoundEvent> getSound();
}
