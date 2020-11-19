package org.samo_lego.golfiv.mixin;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import org.samo_lego.golfiv.utils.Golfer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

import static org.samo_lego.golfiv.GolfIV.golfConfig;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixinCast_golfer implements Golfer {

    private final PlayerEntity player = (PlayerEntity) (Object) this;

    @Unique
    private boolean blockCollisions, entityCollisions;

    @Unique
    private int cheatAttempts = 0;

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

    @Override
    public void punish() {
        if(player instanceof ServerPlayerEntity) {
            ((ServerPlayerEntity) player).networkHandler.disconnect(new LiteralText(
                    "§3[GolfIV]\n§a" +
                            golfConfig.kickMessages.get(new Random().nextInt(golfConfig.kickMessages.size()
                            ))
            ));
        }

        /*player.sendMessage(
                new LiteralText(
                        "§3[GolfIV]\n§a" +
                                golfConfig.kickMessages.get(new Random().nextInt(golfConfig.kickMessages.size())
                                )), false
        );*/
    }

    @Override
    public void setCheatAttepmts(int cheatAttempts) {
        this.cheatAttempts = cheatAttempts;
    }

    @Override
    public int getCheatAttepmts() {
        return this.cheatAttempts;
    }

    @Inject(method = "collideWithEntity(Lnet/minecraft/entity/Entity;)V", at = @At("HEAD"))
    private void updateCollision(Entity entity, CallbackInfo ci) {
        if(entity instanceof BoatEntity) {
            if(entity.equals(this.player.getVehicle())) {
                this.setEntityCollisions(false);
            }
            else if(!this.entityCollisions && !this.player.hasVehicle()) {
                this.setEntityCollisions(true);
            }
        }
    }


    @Inject(method = "Lnet/minecraft/entity/player/PlayerEntity;tick()V", at = @At("TAIL"))
    private void lowerCheatAttempts(CallbackInfo ci) {
        this.cheatAttempts = this.cheatAttempts > 0 ? this.cheatAttempts - 1 : 0;
    }
}
