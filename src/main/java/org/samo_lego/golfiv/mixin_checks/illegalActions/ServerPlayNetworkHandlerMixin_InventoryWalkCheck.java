package org.samo_lego.golfiv.mixin_checks.illegalActions;

import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.samo_lego.golfiv.casts.Golfer;
import org.samo_lego.golfiv.mixin_checks.accessors.PlayerMoveC2SPacketAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.samo_lego.golfiv.GolfIV.golfConfig;
import static org.samo_lego.golfiv.utils.CheatType.ILLEGAL_ACTIONS;

/**
 * Checks if player is doing impossible actions while having GUI (ScreenHandler) open.
 */
@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin_InventoryWalkCheck {

    @Shadow public ServerPlayerEntity player;

    @Unique
    private short illegalActionsMoveAttempts;
    @Unique
    private short illegalActionsLookAttempts;


    /**
     * Sets the status of open GUI to false.
     *
     * @param packet
     * @param ci
     */
    @Inject(
            method = "onCloseHandledScreen(Lnet/minecraft/network/packet/c2s/play/CloseHandledScreenC2SPacket;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;closeScreenHandler()V"
            )
    )
    private void closeHandledScreen(CloseHandledScreenC2SPacket packet, CallbackInfo ci) {
        this.illegalActionsMoveAttempts = 0;
        this.illegalActionsLookAttempts = 0;
        ((Golfer) this.player).setOpenGui(false);
    }

    /**
     * Checks for movement while having a GUI open.
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
    private void checkInventoryWalk(PlayerMoveC2SPacket packet, CallbackInfo ci) {
        if(golfConfig.main.checkIllegalActions) {
            Vec3d packetMovement = new Vec3d(
                    packet.getX(this.player.getX()) - this.player.getX(),
                    packet.getY(this.player.getY()) - this.player.getY(),
                    packet.getZ(this.player.getZ()) - this.player.getZ()
            );
            if(((Golfer) this.player).hasOpenGui() && !player.isFallFlying() && !player.isInsideWaterOrBubbleColumn()) {
                if(packet instanceof PlayerMoveC2SPacket.PositionOnly && packetMovement.getY() == 0 && packetMovement.lengthSquared() != 0) {
                    if(++this.illegalActionsMoveAttempts > 40) {
                        ((Golfer) this.player).report(ILLEGAL_ACTIONS, 10);
                        this.player.requestTeleport(player.getX(), player.getY(), player.getZ());
                        ci.cancel();
                    }
                }
                else if(packet instanceof PlayerMoveC2SPacket.LookOnly || packet instanceof PlayerMoveC2SPacket.Both && ((PlayerMoveC2SPacketAccessor) packet).changesLook()) {
                    if(++this.illegalActionsLookAttempts > 8) {
                        ((Golfer) this.player).report(ILLEGAL_ACTIONS, 50);
                        this.player.requestTeleport(player.getX(), player.getY(), player.getZ());
                        ci.cancel();
                    }
                }
            }
        }
    }

    /**
     * Checks for entity interactions while having a GUI open.
     *
     * @param packet
     * @param ci
     */
    @Inject(
            method = "onPlayerInteractEntity(Lnet/minecraft/network/packet/c2s/play/PlayerInteractEntityC2SPacket;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V",
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    private void entityInteraction(PlayerInteractEntityC2SPacket packet, CallbackInfo ci) {
        if(golfConfig.main.checkIllegalActions && ((Golfer) this.player).hasOpenGui()) {
            ((Golfer) this.player).report(ILLEGAL_ACTIONS, 50);
            ci.cancel();
        }
    }


    /**
     * Checks for messages / commands while having GUI open.
     *
     * @param packet
     * @param ci
     */
    @Inject(
            method = "onGameMessage(Lnet/minecraft/network/packet/c2s/play/ChatMessageC2SPacket;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void chatWithInventoryOpened(ChatMessageC2SPacket packet, CallbackInfo ci) {
        if(golfConfig.main.checkIllegalActions && ((Golfer) player).hasOpenGui()) {
            ((Golfer) this.player).report(ILLEGAL_ACTIONS, 100);
            ci.cancel();
        }
    }
}
