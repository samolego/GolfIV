package org.samo_lego.golfiv.mixin.movement;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.samo_lego.golfiv.GolfIV.golfConfig;


@Mixin(Entity.class)
public abstract class EntityMixin_NetherRoofDamage {

    @Shadow
    protected abstract void tickInVoid();

    @Unique
    private final Entity self = (Entity) (Object) this;

    @Inject(method = "attemptTickInVoid", at = @At("RETURN"))
    private void golfiv_inflictRoofDamage(CallbackInfo ci) {
        if (self instanceof ServerPlayerEntity) {
            System.out.printf("%.2f, %d\n", self.getY(), self.getEntityWorld().getTopY());
        }
        if (golfConfig.main.inflictNetherRoofDamage != -1 &&
                self.getY() >= golfConfig.main.inflictNetherRoofDamage &&
                self.getEntityWorld().getRegistryKey() == World.NETHER) {
            this.tickInVoid();
        }
    }
}
