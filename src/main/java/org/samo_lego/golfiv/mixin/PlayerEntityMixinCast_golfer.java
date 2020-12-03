package org.samo_lego.golfiv.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import org.samo_lego.golfiv.casts.Golfer;
import org.samo_lego.golfiv.utils.BallLogger;
import org.samo_lego.golfiv.utils.CheatType;
import org.spongepowered.asm.mixin.Mixin;
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
    public void report(CheatType cheatType) {
        if(player instanceof ServerPlayerEntity) {
            final ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) player;
            if(golfConfig.logging.toConsole) {
                BallLogger.logInfo(player.getGameProfile().getName() + " is probably using " + cheatType.getCheat() + " hack(s).");
            }
        }

        if(player instanceof ServerPlayerEntity) {
            ((ServerPlayerEntity) player).networkHandler.disconnect(new LiteralText(
                    "§3[GolfIV]\n§a" +
                            golfConfig.kickMessages.get(new Random().nextInt(golfConfig.kickMessages.size()
                            ))
            ));
        }
        int meesages = golfConfig.kickMessages.size();
        if(meesages > 0)
            player.sendMessage(
                    new LiteralText(
                            "§3[GolfIV]\n§a" +
                                    golfConfig.kickMessages.get(
                                            new Random().nextInt(meesages)
                                    )
                    ),
                    false
            );
    }

    @Inject(method = "collideWithEntity(Lnet/minecraft/entity/Entity;)V", at = @At("HEAD"))
    private void updateCollision(Entity entity, CallbackInfo ci) {
        if(entity.isCollidable()) {
            if(entity.equals(this.player.getVehicle())) {
                this.setEntityCollisions(false);
            }
            else if(!this.entityCollisions && !this.player.hasVehicle()) {
                this.setEntityCollisions(true);
            }
        }
    }
}
