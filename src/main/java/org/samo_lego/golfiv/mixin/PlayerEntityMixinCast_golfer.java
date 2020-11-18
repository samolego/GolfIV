package org.samo_lego.golfiv.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import org.samo_lego.golfiv.utils.Golfer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixinCast_golfer implements Golfer {

    private final PlayerEntity player = (PlayerEntity) (Object) this;

    @Unique
    private boolean blockCollisions, entityCollisions;

    @Override
    public boolean isNearGround() {
        return blockCollisions || entityCollisions;
    }

    @Override
    public void setBlockCollisions(boolean blockCollisions) {
        this.blockCollisions = blockCollisions;
    }

    @Override
    public boolean hasBlockCollisions() {
        return blockCollisions;
    }

    @Override
    public void setEntityCollisions(boolean entityCollisions) {
        this.entityCollisions = entityCollisions;
    }

    @Override
    public boolean hasEntityCollisions() {
        return entityCollisions;
    }

    @Inject(method = "collideWithEntity(Lnet/minecraft/entity/Entity;)V", at = @At("HEAD"))
    private void updateCollision(Entity entity, CallbackInfo ci) {
        if(entity instanceof BoatEntity) {
            if(this.player.hasVehicle() && entity.equals(this.player.getVehicle())) {
                if(this.hasEntityCollisions())
                    this.setEntityCollisions(false);
            }
            else if(!this.hasEntityCollisions()) {
                this.setEntityCollisions(true);
            }
        }
    }
}
