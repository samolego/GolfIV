package org.samo_lego.golfiv.mixin_checks.combat;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.samo_lego.golfiv.casts.Golfer;
import org.samo_lego.golfiv.mixin_checks.accessors.EntityVelocityUpdateS2CPacketAccessor;
import org.samo_lego.golfiv.mixin_checks.accessors.PlayerMoveC2SPacketAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.samo_lego.golfiv.GolfIV.golfConfig;
import static org.samo_lego.golfiv.utils.CheatType.ANTIKNOCKBACK;

/**
 * Checks if player has moved after being hit.
 */
@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin_AntiKnockbackCheck {

    @Shadow public ServerPlayerEntity player;

    @Unique
    private boolean checkKnockback;

    @Inject(
            method = "onPlayerMove(Lnet/minecraft/network/packet/c2s/play/PlayerMoveC2SPacket;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;hasVehicle()Z"
            )
    )
    private void checkHitKnockback(PlayerMoveC2SPacket packet, CallbackInfo ci) {
        if(checkKnockback && !((PlayerMoveC2SPacketAccessor) packet).changesPosition()) {
            ((Golfer) player).report(ANTIKNOCKBACK, golfConfig.sus.antiknockback);
        }
        this.checkKnockback = false;
    }


    @Inject(
            method = "sendPacket(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V",
            at = @At(
                    value = "TAIL"
            )
    )
    private void onSend(Packet<?> packet, GenericFutureListener<? extends Future<? super Void>> listener, CallbackInfo ci) {
        if(golfConfig.combat.checkAntiKnockback && !this.checkKnockback && packet instanceof EntityVelocityUpdateS2CPacket && this.player.getEntityId() == ((EntityVelocityUpdateS2CPacketAccessor) packet).getEntityId())
            this.checkKnockback = true;
    }
}
