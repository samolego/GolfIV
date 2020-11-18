package org.samo_lego.golfiv.mixin;

import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
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
public class ServerPlayNetworkHandlerMixin_movementChecks {

    @Shadow public ServerPlayerEntity player;
    @Unique
    private boolean lastOnGround, lLastOnGround;

    @Unique
    private Vec3d lastMovement;

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
        //double packetDist = packet.getY(this.player.getY()) - this.player.getY();
        Vec3d packetMovement = new Vec3d(
                packet.getX(this.player.getX()) - this.player.getX(),
                packet.getY(this.player.getY()) - this.player.getY(),
                packet.getZ(this.player.getZ()) - this.player.getZ()
        );

        Box bBox = player.getBoundingBox().expand(0, 0.25005, 0).offset(0, packetMovement.y - 0.25005, 0);

        Stream<VoxelShape> vs = player.getEntityWorld().getBlockCollisions(player, bBox);
        long blockCollisions = vs.count();
        if(blockCollisions != 0 && ((Golfer) player).hasEntityCollisions()) {
            ((Golfer) player).setEntityCollisions(false);
        }
        ((Golfer) player).setBlockCollisions(blockCollisions != 0);

        boolean onGround = ((Golfer) player).isNearGround();

        if(this.lastMovement == null) {
            this.lastMovement = packetMovement;
            return;
        }

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
                double predictedDeltaY;
                double d = 0.08D;
                boolean notLevitating = this.player.getVelocity().y <= 0.0D;

                if (notLevitating && this.player.hasStatusEffect(StatusEffects.SLOW_FALLING)) {
                    d = 0.01D;
                }


                if (this.player.hasStatusEffect(StatusEffects.LEVITATION))
                    predictedDeltaY = this.lastMovement.y + (0.05D * (double)(this.player.getStatusEffect(StatusEffects.LEVITATION).getAmplifier() + 1) - this.lastMovement.y) * 0.2D;
                else
                    predictedDeltaY = (this.lastMovement.y - d) * 0.9800000190734863D;

                if(Math.abs(predictedDeltaY) >= 0.005D && Math.abs(predictedDeltaY - packetMovement.y) > 0.003) {
                    if(flyAttempts > 4) {
                        flyAttempts = 0;
                        player.sendMessage(
                                new LiteralText(
                                        "§3[GolfIV]\n§a" +
                                                golfConfig.kickMessages.get(new Random().nextInt(golfConfig.kickMessages.size())
                                                )), false
                        );
                        /*player.networkHandler.disconnect(new LiteralText(
                                "§3[GolfIV]\n§a" +
                                        golfConfig.kickMessages.get(new Random().nextInt(golfConfig.kickMessages.size()
                                        ))
                        ));*/
                    }
                    ++flyAttempts;
                }
                else
                    flyAttempts = flyAttempts > 0 ? -1 : 0;
                this.lastMovement = packetMovement;
            }
        }

        this.lLastOnGround = this.lastOnGround;
        this.lastOnGround = onGround;
    }
}
