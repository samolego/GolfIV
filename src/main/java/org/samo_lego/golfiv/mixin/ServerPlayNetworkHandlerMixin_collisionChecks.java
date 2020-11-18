package org.samo_lego.golfiv.mixin;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import org.samo_lego.golfiv.utils.Golfer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;
import java.util.stream.Stream;

import static org.samo_lego.golfiv.GolfIV.golfConfig;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin_collisionChecks {

    @Shadow public ServerPlayerEntity player;
    @Unique
    private boolean lastOnGround, lLastOnGround;

    @Unique
    private double lastDist;

    @Unique
    private double flyAttempts;

    @ModifyVariable(
            method = "onPlayerMove(Lnet/minecraft/network/packet/c2s/play/PlayerMoveC2SPacket;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;getBoundingBox()Lnet/minecraft/util/math/Box;"
            )
    )
    private PlayerMoveC2SPacket checkOnGround(PlayerMoveC2SPacket packet) {
        return packet;
    }

    @Inject(
            method = "onPlayerMove(Lnet/minecraft/network/packet/c2s/play/PlayerMoveC2SPacket;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void checkMovement(PlayerMoveC2SPacket packet, CallbackInfo ci) {
        double packetDist = packet.getY(this.player.getY()) - this.player.getY();
        updateNearGround(packetDist);
        boolean onGround = ((Golfer) player).isNearGround();

        if(!this.lLastOnGround && !this.lastOnGround && !onGround) {
            if(packet.isOnGround() && golfConfig.main.yesFall) {
                ((PlayerMoveC2SPacketAccessor) packet).setOnGround(false);
                /*player.sendMessage(
                        new LiteralText(
                                "§3[GolfIV]\n§a" +
                                        golfConfig.kickMessages.get(new Random().nextInt(golfConfig.kickMessages.size())
                                        )), false
                );*/
                player.networkHandler.disconnect(new LiteralText(
                        "§3[GolfIV]\n§a" +
                                golfConfig.kickMessages.get(new Random().nextInt(golfConfig.kickMessages.size()
                        ))
                ));
            }
            if(golfConfig.main.noFly && !player.abilities.allowFlying) {
                // LivingEntity#travel
                double d = 0.08D;
                /*boolean bl = this.player.getVelocity().y <= 0.0D;
                if (bl && this.player.hasStatusEffect(StatusEffects.SLOW_FALLING)) {
                    d = 0.01D;
                }

                if (this.player.hasStatusEffect(StatusEffects.LEVITATION))
                    this.lastYDist += (0.05D * (double)(this.player.getStatusEffect(StatusEffects.LEVITATION).getAmplifier() + 1) - this.lastYDist) * 0.2D;
                */
                double predictedDist = (this.lastDist - d) * 0.9800000190734863D;

                if(Math.abs(predictedDist) >= 0.005D && Math.abs(predictedDist - packetDist) > 0.003) {
                    if(flyAttempts > 4) {
                        flyAttempts = 0;
                        /*player.sendMessage(
                                new LiteralText(
                                        "§3[GolfIV]\n§a" +
                                                golfConfig.kickMessages.get(new Random().nextInt(golfConfig.kickMessages.size())
                                                )), false
                        );*/
                        player.networkHandler.disconnect(new LiteralText(
                                "§3[GolfIV]\n§a" +
                                        golfConfig.kickMessages.get(new Random().nextInt(golfConfig.kickMessages.size()
                                        ))
                        ));
                    }
                    ++flyAttempts;
                }
                else
                    flyAttempts = flyAttempts > 0 ? -1 : 0;
                this.lastDist = packetDist;
            }
        }

        this.lLastOnGround = this.lastOnGround;
        this.lastOnGround = onGround;
    }

    @Unique
    private void updateNearGround(double offset) {
        Box bBox = player.getBoundingBox().expand(0, 0.25005, 0).offset(0, offset - 0.25005, 0);

        Stream<VoxelShape> vs = player.getEntityWorld().getBlockCollisions(player, bBox);
        long blockCollisions = vs.count();
        if(blockCollisions != 0 && ((Golfer) player).hasEntityCollisions()) {
            ((Golfer) player).setEntityCollisions(false);
        }
        ((Golfer) player).setBlockCollisions(blockCollisions != 0);
    }
}
