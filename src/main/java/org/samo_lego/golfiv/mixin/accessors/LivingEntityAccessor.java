package org.samo_lego.golfiv.mixin.accessors;// Created 2021-30-06T00:07:26

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.TrackedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @author KJP12
 * @since ${version}
 **/
@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Accessor("HEALTH")
    static TrackedData<Float> getHealth() {
        throw new AssertionError("Accessor failed.");
    }
}
