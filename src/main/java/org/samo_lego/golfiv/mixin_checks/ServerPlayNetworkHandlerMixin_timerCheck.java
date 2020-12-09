package org.samo_lego.golfiv.mixin_checks;

import net.minecraft.network.packet.c2s.play.KeepAliveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin_timerCheck {

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
            )
    )
    private void checkTimer(PlayerMoveC2SPacket packet, CallbackInfo ci) {
        /*if(golfConfig.main.antiTimer) {
            long currentPacketTime = System.currentTimeMillis();
            long lastTime = this.lastPacketTime;
            this.lastPacketTime = currentPacketTime;


            if(lastTime != 0) {
                this.packetRate += (50 + lastTime - currentPacketTime);
            }

            if(this.packetRate < -100) {
                this.packetRate = -50;
            }
            else if(this.packetRate > 300) {
                ((Golfer) player).report(TIMER);
                this.packetRate = 0;
            }
            //System.out.println(packetRate);
        }*/
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
        //this.packetRate -= 50;
    }

    /**
     * Tries to detect timer hack.
     *
     * @param packet
     * @param ci
     */
    @Inject(
            method = "onKeepAlive(Lnet/minecraft/network/packet/c2s/play/KeepAliveC2SPacket;)V",
            at = @At("HEAD")
    )
    private void checkTimerWithKeepAlive(KeepAliveC2SPacket packet, CallbackInfo ci) {
        /*if(golfConfig.main.antiTimer) {
            long currentPacketTime = System.currentTimeMillis();
            long lastTime = this.lastPacketTime;
            this.lastPacketTime = currentPacketTime;


            if(lastTime != 0) {
                this.packetRate += (15000 + lastTime - currentPacketTime);
            }

            if(this.packetRate > 300) {
                ((Golfer) player).report(TIMER);
                this.packetRate = 0;
            }
            System.out.println(packetRate);
        }*/
    }
}
