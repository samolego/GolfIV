package org.samo_lego.golfiv.mixin;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.DecoderHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(DecoderHandler.class)
public abstract class DecoderHandlerMixin_timerCheck {

    @Inject(
            method = "decode(Lio/netty/channel/ChannelHandlerContext;Lio/netty/buffer/ByteBuf;Ljava/util/List;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/Packet;read(Lnet/minecraft/network/PacketByteBuf;)V"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void checkForTimer(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list, CallbackInfo ci, PacketByteBuf packetByteBuf, int i, Packet<?> packet) {
        //System.out.println(packet.getClass() + " packet");
    }
}
