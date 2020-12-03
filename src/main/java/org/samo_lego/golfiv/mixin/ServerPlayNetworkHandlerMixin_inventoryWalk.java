package org.samo_lego.golfiv.mixin;

import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.samo_lego.golfiv.casts.Golfer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode.OPEN_INVENTORY;
import static org.samo_lego.golfiv.utils.CheatType.INVENTORY_WALK;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin_inventoryWalk {

    @Shadow public ServerPlayerEntity player;


    @Inject(
            method = "onCloseHandledScreen(Lnet/minecraft/network/packet/c2s/play/CloseHandledScreenC2SPacket;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;closeScreenHandler()V"
            )
    )
    private void closeHandledScreen(CloseHandledScreenC2SPacket packet, CallbackInfo ci) {
        ((Golfer) this.player).setOpenGui(false);
    }

    @Inject(
            method = "onClientCommand(Lnet/minecraft/network/packet/c2s/play/ClientCommandC2SPacket;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;updateLastActionTime()V"
            )
    )
    private void openScreenHandler(ClientCommandC2SPacket packet, CallbackInfo ci) {
        if(packet.getMode() == OPEN_INVENTORY) {
            ((Golfer) this.player).setOpenGui(true);
        }
    }

    @Inject(
            method = "onPlayerMove(Lnet/minecraft/network/packet/c2s/play/PlayerMoveC2SPacket;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V",
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    private void checkInventoryWalk(PlayerMoveC2SPacket packet, CallbackInfo ci) {
        Vec3d packetMovement = new Vec3d(
                packet.getX(this.player.getX()) - this.player.getX(),
                packet.getY(this.player.getY()) - this.player.getY(),
                packet.getZ(this.player.getZ()) - this.player.getZ()
        );
        if(((Golfer) this.player).hasOpenGui() && !player.isFallFlying() && packetMovement.lengthSquared() != 0) {
            ((Golfer) this.player).report(INVENTORY_WALK);
            ci.cancel();
        }
    }
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
        if(((Golfer) this.player).hasOpenGui()) {
            ((Golfer) this.player).report(INVENTORY_WALK);
            ci.cancel();
        }
    }

    @Inject(
            method = "onGameMessage(Lnet/minecraft/network/packet/c2s/play/ChatMessageC2SPacket;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void chatWithInventoryOpened(ChatMessageC2SPacket packet, CallbackInfo ci) {
        if(((Golfer) player).hasOpenGui()) {
            ((Golfer) this.player).report(INVENTORY_WALK);
            ci.cancel();
        }
    }
}
