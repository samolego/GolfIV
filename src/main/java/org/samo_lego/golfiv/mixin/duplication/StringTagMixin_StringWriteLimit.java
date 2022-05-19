package org.samo_lego.golfiv.mixin.duplication;

import net.minecraft.nbt.NbtString;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.DataOutput;
import java.nio.charset.StandardCharsets;

import static org.samo_lego.golfiv.GolfIV.golfConfig;

@Mixin(NbtString.class)
public class StringTagMixin_StringWriteLimit {
    @Mutable
    @Shadow
    @Final
    private String value;

    @Inject(method = "write(Ljava/io/DataOutput;)V", at = @At("HEAD"))
    private void raiseStringLimit(DataOutput output, CallbackInfo ci) {
        if (golfConfig.duplication.patchSaveLimit) {
            byte[] data = this.value.getBytes(StandardCharsets.UTF_8);

            if (data.length > 65535) // DataOutputStream limit
                this.value = new String(data, 0, 65534);
        }
    }
}
