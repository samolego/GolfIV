package org.samo_lego.golfiv.mixin;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
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
public abstract class ServerPlayNetworkHandlerMixin_timerCheck {

    @Shadow public ServerPlayerEntity player;
    @Shadow private int ticks;
    @Unique
    private long lastPacketTime = 0;
    @Unique
    private double packetRate = 0;
    @Unique
    private boolean lastStill, lLastStill;

    /**
     * Tries to detect timer hack.
     *
     * Bad.
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
        if(golfConfig.main.antiTimer) {
            long currentPacketTime = System.currentTimeMillis();
            long lastTime = this.lastPacketTime;
            this.lastPacketTime = currentPacketTime;

            Vec3d packetMovement = new Vec3d(
                    packet.getX(this.player.getX()) - this.player.getX(),
                    packet.getY(this.player.getY()) - this.player.getY(),
                    packet.getZ(this.player.getZ()) - this.player.getZ()
            );
            Vec2f packetLook = new Vec2f(
                    packet.getYaw(this.player.yaw) - this.player.yaw,
                    packet.getPitch(this.player.pitch) - this.player.pitch
            );
            double delay;
            boolean isStill = packetMovement.lengthSquared() == 0;
            if(isStill /*&& this.lastStill  && this.lLastStill */&& packet instanceof PlayerMoveC2SPacket.PositionOnly) {
                // One second passed from previous packet
                //System.out.println("Standing still.");
                delay = 1000.0D;
            }
            else
                delay = 50.0D;

            this.lLastStill = this.lastStill;
            this.lastStill = isStill;

            if(lastTime != 0.0D) {
                this.packetRate += (delay + lastTime - currentPacketTime);
            }
            //System.out.println("Rate: " + this.packetRate);

            if(this.packetRate > 2000.0D) {
                ((Golfer) player).report(TIMER);
                this.packetRate = 0.0D;
            }
            else if(this.packetRate < -200.0D) {
                this.packetRate = -100.0D;
            }
        }
    }

    /**
     * Re-balances timer on teleport request.
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
        this.packetRate -= 50.0D;
        //System.out.println("TP");
    }
}
