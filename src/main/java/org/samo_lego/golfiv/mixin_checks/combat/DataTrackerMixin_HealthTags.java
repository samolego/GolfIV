package org.samo_lego.golfiv.mixin_checks.combat;

import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.network.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static org.samo_lego.golfiv.GolfIV.golfConfig;

@Mixin(DataTracker.class)
public class DataTrackerMixin_HealthTags {

    @Inject(
            method = "writeEntryToPacket",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/PacketByteBuf;writeByte(I)Lio/netty/buffer/ByteBuf;"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true
    )
    private static void removeHealthTag(PacketByteBuf packetByteBuf, DataTracker.Entry<?> entry, CallbackInfo ci, TrackedData<?> trackedData, int i) {
        // https://wiki.vg/Entity_metadata#Living_Entity ID 8 is entity's health
        if(golfConfig.main.removeHealthTags && trackedData.getId() == 8) {
            ci.cancel();
        }
    }
}
