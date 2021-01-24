package org.samo_lego.golfiv.mixin_checks.S2CPacket;

import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.samo_lego.golfiv.mixin_checks.accessors.EntityPositionS2CPacketAccessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.samo_lego.golfiv.GolfIV.golfConfig;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin_EntityTeleportData {

    @Shadow
    public ServerPlayerEntity player;

    @Shadow @Final private MinecraftServer server;

    /**
     * If player teleports out of render distance, we modify the coordinates of the
     * packet, in order to hide player's original TP coordinates.
     *
     * @param packet
     * @param ci
     */
    @Inject(
            method = "sendPacket(Lnet/minecraft/network/Packet;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void removeTeleportData(Packet<?> packet, CallbackInfo ci) {
        if(golfConfig.packet.removeTeleportData && packet instanceof EntityPositionS2CPacket) {
            EntityPositionS2CPacketAccessor packetAccessor = (EntityPositionS2CPacketAccessor) packet;

            // Similar to ServerPlayNetworkHandlerMixin_SoundExploit#patchSoundCoordinates
            int maxPlayerDistance = server.getPlayerManager().getViewDistance() * 16;
            double deltaX = player.getX() - packetAccessor.getX();
            double deltaZ = player.getZ() - packetAccessor.getZ();

            double actualPlayerDistance = deltaX * deltaZ;

            if(maxPlayerDistance * maxPlayerDistance < Math.abs(actualPlayerDistance)) {
                // Can not track this entity (teleporter), why send data?
                packetAccessor.setX(player.getX() + maxPlayerDistance);
                packetAccessor.setZ(player.getZ() + maxPlayerDistance);
            }
        }
    }
}
