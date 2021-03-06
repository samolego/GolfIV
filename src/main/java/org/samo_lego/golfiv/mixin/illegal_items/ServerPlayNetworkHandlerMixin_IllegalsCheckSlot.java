package org.samo_lego.golfiv.mixin.illegal_items;

import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.samo_lego.golfiv.casts.Golfer;
import org.samo_lego.golfiv.casts.ItemStackChecker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.samo_lego.golfiv.GolfIV.golfConfig;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin_IllegalsCheckSlot {

    @Shadow public ServerPlayerEntity player;

    /**
     * Checks whether the clicked slot contains
     * illegal stack.
     *
     * Also sets the GUI status to open.
     */
    @Inject(
            method = "onClickSlot(Lnet/minecraft/network/packet/c2s/play/ClickSlotC2SPacket;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;updateLastActionTime()V"
            )
    )
    private void checkSlot(ClickSlotC2SPacket packet, CallbackInfo ci) {
        ((Golfer) player).setOpenGui(golfConfig.main.checkInventoryActions);
        int packetSlot = packet.getSlot();
        if(packetSlot >= 0) {
            ItemStack itemStack = this.player.currentScreenHandler.getSlot(packetSlot).getStack();
            //noinspection ConstantConditions
            ((ItemStackChecker) (Object) itemStack).makeLegal(!this.player.isCreative());
        }
    }
}
