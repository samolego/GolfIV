package org.samo_lego.golfiv.mixin_checks.combat;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.samo_lego.golfiv.casts.Golfer;
import org.samo_lego.golfiv.utils.BallLogger;
import org.samo_lego.golfiv.utils.CheatType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;

import static org.samo_lego.golfiv.GolfIV.golfConfig;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin_KillauraAccuracyCheck {

    @Shadow public ServerPlayerEntity player;
    @Unique
    private boolean wasLastHit;

    @Unique
    private int handSwings, entityHits;


    @Inject(
            method = "onPlayerInteractEntity(Lnet/minecraft/network/packet/c2s/play/PlayerInteractEntityC2SPacket;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;attack(Lnet/minecraft/entity/Entity;)V"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true
    )
    private void onHitEntity(PlayerInteractEntityC2SPacket packet, CallbackInfo ci, Entity victim, Hand hand, ItemStack weapon, Optional<ActionResult> optional) {
        if(golfConfig.combat.checkKillaura) {
            if(this.wasLastHit) {
                ((Golfer) player).report(CheatType.NO_HAND_SWING, golfConfig.sus.noHandSwing);
                ci.cancel();
            }
            this.wasLastHit = true;
            ++this.entityHits;

            if(this.handSwings >= 50) {
                if(golfConfig.main.developerMode)
                    BallLogger.logInfo(entityHits + " hits of " + handSwings + " tries.");

                ((Golfer) player).setHitAccuracy(entityHits, handSwings);
                if(golfConfig.main.developerMode)
                    BallLogger.logInfo(((Golfer) player).getHitAccuracy() + "% accuracy.");
                this.handSwings = 0;
                this.entityHits = 0;
            }
        }
    }

    @Inject(
            method = "onHandSwing(Lnet/minecraft/network/packet/c2s/play/HandSwingC2SPacket;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;updateLastActionTime()V"
            )
    )
    private void onHandSwing(HandSwingC2SPacket packet, CallbackInfo ci) {
        if(golfConfig.combat.checkKillaura) {
            this.wasLastHit = false;
            ++this.handSwings;
        }
    }
}
