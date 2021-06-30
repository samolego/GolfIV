package org.samo_lego.golfiv.mixin.accessors;// Created 2021-30-06T00:10:45

import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @author KJP12
 * @since ${version}
 **/
@Mixin(PlayerEntity.class)
public interface PlayerEntityAccessor {
    @Accessor("ABSORPTION_AMOUNT")
    static TrackedData<Float> getAbsorption() {
        throw new AssertionError("Accessor failed.");
    }
}
