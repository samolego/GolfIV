package org.samo_lego.golfiv.mixin_checks.movement;

import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.samo_lego.golfiv.casts.Golfer;
import org.samo_lego.golfiv.casts.NetworkHandlerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerPlayNetworkHandler.class, priority = 700)
public class ServerPlayNetworkHandlerMixinCast_NetworkHandlerData implements NetworkHandlerData {
    @Shadow public ServerPlayerEntity player;
    @Unique
    private Vec3d packetMovement;
    @Unique
    private boolean lastOnGround;
    @Unique
    private boolean lLastOnGround;

    @Override
    public boolean wasLastOnGround() {
        return this.lastOnGround;
    }

    @Override
    public boolean wasLLastOnGround() {
        return this.lLastOnGround;
    }

    @Override
    public Vec3d getPacketMovement() {
        return this.packetMovement;
    }

    @Inject(method = "onPlayerMove(Lnet/minecraft/network/packet/c2s/play/PlayerMoveC2SPacket;)V", at = @At("RETURN"))
    private void setLastPacketValues(PlayerMoveC2SPacket packet, CallbackInfo ci) {
        this.lLastOnGround = this.lastOnGround;
        this.lastOnGround = ((Golfer) this.player).isNearGround();
    }

    @Inject(
            method = "onPlayerMove(Lnet/minecraft/network/packet/c2s/play/PlayerMoveC2SPacket;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;hasVehicle()Z"
            )
    )
    private void setPacketMovement(PlayerMoveC2SPacket packet, CallbackInfo ci) {
        if(!player.hasVehicle() || player.getRootVehicle() instanceof LivingEntity)
            this.packetMovement = new Vec3d(
                    packet.getX(this.player.getX()) - this.player.getX(),
                    packet.getY(this.player.getY()) - this.player.getY(),
                    packet.getZ(this.player.getZ()) - this.player.getZ()
            );
    }
}
