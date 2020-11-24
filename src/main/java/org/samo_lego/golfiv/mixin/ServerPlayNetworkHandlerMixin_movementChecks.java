package org.samo_lego.golfiv.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import org.samo_lego.golfiv.utils.CheatType;
import org.samo_lego.golfiv.utils.casts.Golfer;
import org.samo_lego.golfiv.utils.casts.TPSTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.stream.Stream;

import static org.samo_lego.golfiv.GolfIV.golfConfig;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin_movementChecks {

    private final ServerPlayNetworkHandler serverPlayNetworkHandler = (ServerPlayNetworkHandler) (Object) this;

    @Shadow public ServerPlayerEntity player;
    @Unique
    private boolean lastOnGround, lLastOnGround;

    @Unique
    private Vec3d lastMovement;

    @Unique
    private int flyAttempts = 0;
    @Unique
    private int jumpFP = 0;
    @Unique
    private int speedFP = 0;

    @Unique
    private double lastDist;

    @Inject(
            method = "onPlayerMove(Lnet/minecraft/network/packet/c2s/play/PlayerMoveC2SPacket;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void checkMovement(PlayerMoveC2SPacket packet, CallbackInfo ci) {
        //System.out.println(((TPSTracker) serverPlayNetworkHandler).getAverageTPS());
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

        Entity entity = player.getVehicle();

        if(entity == null) {
            entity = player;
        }

        boolean notLevitating = entity.getVelocity().y <= 0.0D;

        BlockPos blockPos = new BlockPos(entity.getX(), entity.getBoundingBox().minY - 0.5001D, entity.getZ());
        float slipperiness = this.player.getEntityWorld().getBlockState(blockPos).getBlock().getSlipperiness();
        float friction = !this.lastOnGround && !onGround ? 0.91F : slipperiness * 0.91F;

        double predictedDist = this.lastDist * friction;
        double packetDist = packetMovement.x * packetMovement.x + packetMovement.z * packetMovement.z;


        if(!this.lLastOnGround && !this.lastOnGround && !onGround) {
            if((packetDist - predictedDist) > 0.00750716D  && !player.isFallFlying()) {
                if(this.speedFP > 10) {
                    this.speedFP = 0;
                    ((Golfer) player).report(CheatType.SPEED_HACK);
                }
                ++this.speedFP;
                //System.out.println(this.speedFP);
            }

            if(packet.isOnGround() && golfConfig.main.yesFall) {
                ((PlayerMoveC2SPacketAccessor) packet).setOnGround(false);
                if(notLevitating)
                    ((Golfer) player).report(CheatType.NO_FALL);
            }

            if(golfConfig.main.noFly && !player.abilities.allowFlying && !player.isClimbing() && !player.isFallFlying()) {
                double d = 0.08D;

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
                            ((Golfer) player).report(CheatType.FLY_HACK);
                        }
                        ++flyAttempts;
                    }
                    else
                        flyAttempts = 0;
                }
            }
        }
        else if(notLevitating && onGround) {
            if(this.lLastOnGround && this.lastOnGround) {
                // Hopefully fixes some lag issues
                double distDelta = (packetDist - predictedDist) * ((TPSTracker) serverPlayNetworkHandler).getAverageTPS() / 20;

                if(player.hasStatusEffect(StatusEffects.SPEED)) {
                    if(packet.isOnGround() && distDelta > (0.35  + player.getStatusEffect(StatusEffects.SPEED).getAmplifier() * 0.4)) {
                        if(this.jumpFP > 10){
                            this.jumpFP = 0;
                            ((Golfer) player).report(CheatType.SPEED_HACK);
                        }
                        ++this.jumpFP;
                    }
                    else if(distDelta > (0.05394  + player.getStatusEffect(StatusEffects.SPEED).getAmplifier() * 0.02)) {
                        if(this.jumpFP > 10){
                            this.jumpFP = 0;
                            ((Golfer) player).report(CheatType.SPEED_HACK);
                        }
                        ++this.jumpFP;
                    }
                }
                else if(distDelta > 0.0372247D && !((Golfer) player).hasEntityCollisions()) {
                    if(this.jumpFP > 10){
                        this.jumpFP = 0;
                        ((Golfer) player).report(CheatType.SPEED_HACK);
                    }
                    ++this.jumpFP;
                }
                flyAttempts = 0;
            }
            else if (!packet.isOnGround()) {
                this.jumpFP = 0;
            }
            //System.out.println(this.jumpFP);
        }

        this.lastDist = packetDist;
        this.lastMovement = packetMovement;
        this.lLastOnGround = this.lastOnGround;
        this.lastOnGround = onGround;
    }
}
