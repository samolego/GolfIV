package org.samo_lego.golfiv.mixin_checks.accessors;

import net.minecraft.entity.data.DataTracker;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(EntityTrackerUpdateS2CPacket.class)
public interface EntityTrackerUpdateS2CPacketAccessor {
    @Accessor("id")
    int getID();

    @Accessor("trackedValues")
    List<DataTracker.Entry<?>> getTrackedValues();
}
