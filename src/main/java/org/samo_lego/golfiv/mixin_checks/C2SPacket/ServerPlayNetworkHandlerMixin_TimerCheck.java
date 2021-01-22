package org.samo_lego.golfiv.mixin_checks.C2SPacket;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.samo_lego.golfiv.casts.Golfer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

import static org.samo_lego.golfiv.GolfIV.golfConfig;
import static org.samo_lego.golfiv.utils.CheatType.TIMER;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin_TimerCheck {

    @Shadow public ServerPlayerEntity player;
    @Unique
    private long lastPacketTime = 0;
    @Unique
    private int packetRate = 0;

    /**
     * Tries to detect timer hack.
     *
     * @param packet
     * @param ci
     */
    @Inject(
            method = "onPlayerMove(Lnet/minecraft/network/packet/c2s/play/PlayerMoveC2SPacket;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;hasVehicle()Z"
            ),
            cancellable = true
    )
    private void checkTimer(PlayerMoveC2SPacket packet, CallbackInfo ci) {
        if(golfConfig.movement.antiTimer) {
            if(packet instanceof PlayerMoveC2SPacket.Both || packet instanceof PlayerMoveC2SPacket.LookOnly || packet.getX(player.getX()) != player.getX() || packet.getY(player.getY()) != player.getY() || packet.getZ(player.getZ()) != player.getZ()) {
                long currentPacketTime = System.currentTimeMillis();
                long lastTime = this.lastPacketTime;
                this.lastPacketTime = currentPacketTime;

                if(lastTime != 0) {
                    this.packetRate += (50 + lastTime - currentPacketTime);
                }

                if(this.packetRate > 250) {
                    ((Golfer) player).report(TIMER, golfConfig.sus.timer);
                    this.packetRate = 0;

                    this.player.requestTeleport(player.getX(), player.getY(), player.getZ());
                    ci.cancel();
                }
            }
            else {
                this.packetRate = 0;
                this.lastPacketTime = 0;
            }
        }
    }

    /**
     * Re-balances timer on teleport request.
     *
     * @param x
     * @param y
     * @param z
     * @param yaw
     * @param pitch
     * @param set
     * @param ci
     */
    @Inject(method = "teleportRequest(DDDFFLjava/util/Set;)V", at = @At("HEAD"))
    private void timerRebalance(double x, double y, double z, float yaw, float pitch, Set<PlayerPositionLookS2CPacket.Flag> set, CallbackInfo ci) {
        this.packetRate -= 50;
    }
}
