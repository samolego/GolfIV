package org.samo_lego.golfiv.mixin_checks.illegalActions;

import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.samo_lego.golfiv.casts.Golfer;
import org.samo_lego.golfiv.mixin_checks.accessors.EntityAccessor;
import org.samo_lego.golfiv.utils.CheatType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.samo_lego.golfiv.GolfIV.golfConfig;
import static org.samo_lego.golfiv.utils.CheatType.PORTAL_HACK;

/**
 * Checks for PortalHack
 */
@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin_PortalsMessageCheck {
    @Shadow
    public ServerPlayerEntity player;

    /**
     * Checks if the player is using a cheat
     * that allows them to send chat messages
     * while in nether portals
     *
     * @param packet
     * @param ci
     */
    @Inject(
            method = "onGameMessage(Lnet/minecraft/network/packet/c2s/play/ChatMessageC2SPacket;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void stopMessageSendInPortal(ChatMessageC2SPacket packet, CallbackInfo ci) {
        if (golfConfig.packet.checkPortalHack  && ((EntityAccessor) player).inNetherPortal()) {
            ((Golfer) player).report(PORTAL_HACK, golfConfig.sus.portalHack);
            ci.cancel();
        }
    }
}