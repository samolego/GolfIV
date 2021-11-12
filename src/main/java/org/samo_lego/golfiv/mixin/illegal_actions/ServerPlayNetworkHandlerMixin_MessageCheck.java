package org.samo_lego.golfiv.mixin.illegal_actions;

import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.samo_lego.golfiv.casts.Golfer;
import org.samo_lego.golfiv.mixin.accessors.EntityAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.samo_lego.golfiv.GolfIV.golfConfig;

/**
 * Checks for PortalHack
 */
@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin_MessageCheck {
    @Shadow
    public ServerPlayerEntity player;

    /**
     * Checks if the player is using a cheat
     * that allows them to send chat messages
     * while in nether portals / having GUI open.
     */
    @Inject(
            method = "onChatMessage(Lnet/minecraft/network/packet/c2s/play/ChatMessageC2SPacket;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void stopMessageSend(ChatMessageC2SPacket packet, CallbackInfo ci) {
        if ((golfConfig.main.checkInventoryActions && ((Golfer) player).hasOpenGui()) || (golfConfig.packet.checkPortalHack && ((EntityAccessor) player).inNetherPortal())) {
            ci.cancel();
        }
    }
}