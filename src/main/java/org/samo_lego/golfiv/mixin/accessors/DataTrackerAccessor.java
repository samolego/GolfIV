package org.samo_lego.golfiv.mixin.accessors;// Created 2021-30-06T12:22:15

import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @author Ampflower
 **/
@Mixin(DataTracker.class)
public interface DataTrackerAccessor {
    @Accessor
    Entity getTrackedEntity();
}
