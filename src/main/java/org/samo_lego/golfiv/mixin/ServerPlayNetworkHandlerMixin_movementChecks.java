package org.samo_lego.golfiv.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
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
    private int flyAttempts, speedAttempts;

    @Unique
    private double lastDist;

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

        Box bBox = player.getBoundingBox().expand(0, 0.25005D, 0).offset(0, packetMovement.y - 0.25005D, 0);

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
                ((Golfer) player).punish();
            }
            if(golfConfig.main.noFly && !player.abilities.allowFlying && !player.isClimbing()) {
                double d = 0.08D;
                boolean notLevitating = this.player.getVelocity().y <= 0.0D;

                if (notLevitating && this.player.hasStatusEffect(StatusEffects.SLOW_FALLING)) {
                    d = 0.01D;
                }

                if(player.isInLava()) {

                }
                else if(player.isTouchingWater()) {

                }
                else {
                    // LivingEntity#travel
                    double predictedDeltaY;

                    if (this.player.hasStatusEffect(StatusEffects.LEVITATION))
                        predictedDeltaY = this.lastMovement.y + (0.05D * (double)(this.player.getStatusEffect(StatusEffects.LEVITATION).getAmplifier() + 1) - this.lastMovement.y) * 0.2D;
                    else
                        predictedDeltaY = (this.lastMovement.y - d) * 0.9800000190734863D;

                    if(Math.abs(predictedDeltaY) >= 0.005D && Math.abs(predictedDeltaY - packetMovement.y) > 0.003) {
                        if(flyAttempts > 2) {
                            flyAttempts = 0;
                            ((Golfer) player).punish();
                        }
                        ++flyAttempts;
                    }
                    else
                        flyAttempts = flyAttempts > 0 ? -1 : 0;
                }

            }
        }

        Entity entity = player.getVehicle();

        if(entity == null) {
            entity = player;
        }
        BlockPos blockPos = new BlockPos(entity.getX(), entity.getBoundingBox().minY - 0.5001D, entity.getZ());
        float slipperiness = this.player.getEntityWorld().getBlockState(blockPos).getBlock().getSlipperiness();
        float friction = !this.lastOnGround && !onGround ? 0.91F : slipperiness * 0.91F;

        double predictedDist = this.lastDist * friction;
        double packetDist = packetMovement.x * packetMovement.x + packetMovement.z * packetMovement.z;

        if(!this.lLastOnGround  && !this.lastOnGround && !onGround) {
            if((packetDist - predictedDist) > 0.00750716D) {
                ((Golfer) player).punish();
                System.out.println(packetDist - predictedDist);
            }
        }
        else if(this.lLastOnGround && this.lastOnGround && onGround &&packet.isOnGround() && (packetDist - predictedDist) > 0.0372247D) {
            int ca = ((Golfer) player).getCheatAttepmts();
            if(ca > 25) {
                ((Golfer) player).punish();
            }
            ((Golfer) player).setCheatAttepmts(ca + 10);
        }

        this.lastDist = packetDist;




        this.lastMovement = packetMovement;
        this.lLastOnGround = this.lastOnGround;
        this.lastOnGround = onGround;
    }


    @Unique
    private static Vec3d movementInputToVelocity(Vec3d movementInput, float speed, float yaw) {
        double d = movementInput.lengthSquared();
        if (d < 1.0E-7D) {
            return Vec3d.ZERO;
        } else {
            Vec3d vec3d = (d > 1.0D ? movementInput.normalize() : movementInput).multiply((double)speed);
            float f = MathHelper.sin(yaw * 0.017453292F);
            float g = MathHelper.cos(yaw * 0.017453292F);
            return new Vec3d(vec3d.x * (double)g - vec3d.z * (double)f, vec3d.y, vec3d.z * (double)g + vec3d.x * (double)f);
        }
    }
}
