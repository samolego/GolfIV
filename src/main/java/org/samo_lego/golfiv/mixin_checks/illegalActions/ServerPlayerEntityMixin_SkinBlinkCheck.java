package org.samo_lego.golfiv.mixin_checks.illegalActions;

import net.minecraft.network.packet.c2s.play.ClientSettingsC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import org.samo_lego.golfiv.casts.Golfer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.samo_lego.golfiv.GolfIV.golfConfig;
import static org.samo_lego.golfiv.utils.CheatType.SKIN_BLINKER;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin_SkinBlinkCheck {

    public ServerPlayerEntity player;

    @Inject(
            method = "setClientSettings(Lnet/minecraft/network/packet/c2s/play/ClientSettingsC2SPacket;)V",
            at = @At("HEAD")
    )
    private void checkSkinBlink(ClientSettingsC2SPacket packet, CallbackInfo ci) {
        if(golfConfig.packet.checkSkinBlink && ((Golfer) player).hasOpenGui()) {
            ((Golfer) player).report(SKIN_BLINKER, golfConfig.weights.skinBlinker);
        }
    }
}
