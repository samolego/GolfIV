package org.samo_lego.golfiv.mixin;

import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.samo_lego.golfiv.casts.Golfer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode.OPEN_INVENTORY;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin_inventoryWalk {

    @Shadow public ServerPlayerEntity player;
    @Unique
    private boolean hasOpenInventory = false;

    @Inject(
            method = "onCloseHandledScreen(Lnet/minecraft/network/packet/c2s/play/CloseHandledScreenC2SPacket;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;closeScreenHandler()V"
            )
    )
    private void closeHandledScreen(CloseHandledScreenC2SPacket packet, CallbackInfo ci) {
        System.out.println("Closing inv. Was open: " + hasOpenInventory);
        ((Golfer) this.player).setOpenGui(false);
        this.hasOpenInventory = false;
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
            System.out.println("Opening inv. Was open: " + hasOpenInventory);
            ((Golfer) this.player).setOpenGui(true);
            this.hasOpenInventory = true;
        }
    }
}
