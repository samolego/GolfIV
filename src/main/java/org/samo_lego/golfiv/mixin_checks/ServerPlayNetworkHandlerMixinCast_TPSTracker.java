package org.samo_lego.golfiv.mixin_checks;

import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.samo_lego.golfiv.casts.TPSTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixinCast_TPSTracker implements TPSTracker {
    @Unique
    private double msptSum = 0.0F;
    @Unique
    private double lastMillis = 0;
    @Unique
    private short timer = 0;
    @Unique
    private byte mspt = 50;


    @Override
    public double getAverageTPS() {
        return 1000.0 / this.mspt;
    }

    /**
     * Tracks server's tps.
     * @param ci
     */
    @Inject(method = "tick()V", at= @At("HEAD"))
    private void measureTPS(CallbackInfo ci) {
        final double now = System.currentTimeMillis();
        if(this.timer++ > 300) {
            // Get mspt of last 15 seconds (15 sec * 20 ticks/sec => 300)
            this.mspt = (byte) (this.msptSum / 300);
            this.msptSum = 0.0F;
            this.timer = 0;
        }
        this.msptSum += now - this.lastMillis;
        this.lastMillis = now;
    }
}
