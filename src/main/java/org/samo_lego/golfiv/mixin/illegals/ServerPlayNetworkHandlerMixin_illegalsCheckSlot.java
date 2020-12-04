package org.samo_lego.golfiv.mixin.illegals;

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
import static org.samo_lego.golfiv.utils.CheatType.NBT_ITEMS;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin_illegalsCheckSlot {

    @Shadow public ServerPlayerEntity player;

    /**
     * Checks whether the clicked slot contains
     * illegal stack.
     *
     * Also set the GUI status to open.
     *
     * @param packet
     * @param ci
     */
    @Inject(
            method = "onClickSlot(Lnet/minecraft/network/packet/c2s/play/ClickSlotC2SPacket;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;updateLastActionTime()V"
            )
    )
    private void checkSlot(ClickSlotC2SPacket packet, CallbackInfo ci) {
        ((Golfer) player).setOpenGui(true);
        int packetSlot = packet.getSlot();
        if(packetSlot >= 0) {
            ItemStack itemStack = this.player.currentScreenHandler.getSlot(packetSlot).getStack();
            //noinspection ConstantConditions
            boolean illegal = ((ItemStackChecker) (Object) itemStack).makeLegal();
            if(golfConfig.main.checkForStrangeItems && illegal) {
                ((Golfer) this.player).report(NBT_ITEMS);
            }
        }
    }
}
