package org.samo_lego.golfiv.mixin_checks.accessors;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Accessor("inNetherPortal")
    boolean inNetherPortal();
}