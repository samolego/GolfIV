package org.samo_lego.golfiv.mixin.packets;

import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import org.samo_lego.golfiv.event.S2CPacket.S2CPacketCallback;
import org.samo_lego.golfiv.event.combat.EntityInteractPacketCallback;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin_PacketEvents {

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
    private void onPacket(Packet<?> packet, CallbackInfo ci) {
        S2CPacketCallback.EVENT.invoker().preSendPacket(packet, player, server);
    }

    /**
     * Checks whether player is hitting entity through wall
     * by comparing raycast distance of the block and targeted entity.
     *
     * Checks distance from attacker to attacked entity as well,
     * in order to prevent reach hacks.
     *
     * Also checks the angle at which player is hitting the entity.
     *
     * @param packet
     * @param ci
     * @param serverWorld
     * @param victim
     * @param distanceSquared
     */
    @Inject(
            method = "onPlayerInteractEntity(Lnet/minecraft/network/packet/c2s/play/PlayerInteractEntityC2SPacket;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/packet/c2s/play/PlayerInteractEntityC2SPacket;handle(Lnet/minecraft/network/packet/c2s/play/PlayerInteractEntityC2SPacket$Handler;)V"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true
    )
    private void entityInteractCheck(PlayerInteractEntityC2SPacket packet, CallbackInfo ci, ServerWorld serverWorld, Entity victim, double distanceSquared) {
        ActionResult result = EntityInteractPacketCallback.EVENT.invoker().onEntityInteractPacket(this.player, victim, distanceSquared);
        if(result == ActionResult.FAIL) {
            ci.cancel();
        }
    }
}
