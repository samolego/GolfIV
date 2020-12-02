package org.samo_lego.golfiv.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import org.samo_lego.golfiv.utils.CheatType;
import org.samo_lego.golfiv.casts.Golfer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.stream.Stream;

import static org.samo_lego.golfiv.GolfIV.golfConfig;
import static org.samo_lego.golfiv.utils.CheatType.*;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin_movementChecks {

    private final ServerPlayNetworkHandler serverPlayNetworkHandler = (ServerPlayNetworkHandler) (Object) this;

    @Shadow public ServerPlayerEntity player;
    @Shadow private double lastTickX;
    @Shadow private double lastTickY;
    @Shadow private double lastTickZ;
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
    private int elytraFP = 0;

    @Unique
    private double lastDist;
    @Unique
    private CheatType lastCheat;

    public ServerPlayNetworkHandlerMixin_movementChecks() {
    }

    @Inject(
            method = "onPlayerMove(Lnet/minecraft/network/packet/c2s/play/PlayerMoveC2SPacket;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V",
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    private void checkMovement(PlayerMoveC2SPacket packet, CallbackInfo ci) {
        /*System.out.println("Playermove");
        //System.out.println(((TPSTracker) serverPlayNetworkHandler).getAverageTPS());
        //double packetDist = packet.getY(this.player.getY()) - this.player.getY();
        boolean shouldCancel = false;
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

        boolean notLevitating = player.getVelocity().y <= 0.0D;

        BlockPos blockPos = new BlockPos(player.getX(), bBox.minY, player.getZ());
        float slipperiness = this.player.getEntityWorld().getBlockState(blockPos).getBlock().getSlipperiness();
        float friction = !this.lastOnGround && !onGround ? 0.91F : slipperiness * 0.91F;

        double predictedDist = this.lastDist * friction;
        double packetDist = packetMovement.x * packetMovement.x + packetMovement.z * packetMovement.z;
        double distDelta = packetDist - predictedDist;

        if(!this.lLastOnGround && !this.lastOnGround && !onGround) {
            if(packet.isOnGround() && golfConfig.main.yesFall) {
                ((PlayerMoveC2SPacketAccessor) packet).setOnGround(false);
                if(notLevitating)
                    ((Golfer) this.player).report(CheatType.NO_FALL);
            }
            if(this.player.isFallFlying()) {
                if(packetMovement.equals(this.lastMovement) || packetMovement.length() == 0 || this.player.getVelocity().length() == 0) {
                    ++this.elytraFP;
                    if(this.elytraFP > 4) {
                        this.elytraFP = 0;
                        ((Golfer) this.player).report(CheatType.ELYTRA_HACK);
                    }
                }
                else
                    this.elytraFP = 0;
            }
            else {
                if(distDelta > 0.00750716D) {
                    if(this.speedFP > 10) {
                        this.speedFP = 0;
                        shouldCancel = true;
                        ((Golfer) this.player).report(CheatType.SPEED_HACK);
                    }
                    ++this.speedFP;
                    //System.out.println(this.speedFP);
                }

                if(golfConfig.main.noFly && !this.player.abilities.allowFlying && !this.player.isClimbing()) {
                    double d = 0.08D;

                    if (notLevitating && this.player.hasStatusEffect(StatusEffects.SLOW_FALLING)) {
                        d = 0.01D;
                    }

                    if(this.player.isInLava()) {

                    }
                    else if(this.player.isTouchingWater()) {

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
                                shouldCancel = true;
                                ((Golfer) this.player).report(CheatType.FLY_HACK);
                            }
                            ++flyAttempts;
                        }
                        else
                            flyAttempts = 0;
                    }
                }
            }

        }
        else if(notLevitating && onGround) {
            if(this.lLastOnGround && this.lastOnGround) {
                if(this.player.hasStatusEffect(StatusEffects.SPEED)) {
                    if(packet.isOnGround() && distDelta > (0.35  + this.player.getStatusEffect(StatusEffects.SPEED).getAmplifier() * 0.4)) {
                        if(this.jumpFP > 10){
                            this.jumpFP = 0;
                            shouldCancel = true;
                            ((Golfer) this.player).report(CheatType.SPEED_HACK);
                        }
                        ++this.jumpFP;
                    }
                    else if(distDelta > (0.05394  + this.player.getStatusEffect(StatusEffects.SPEED).getAmplifier() * 0.02)) {
                        if(this.jumpFP > 10){
                            this.jumpFP = 0;
                            shouldCancel = true;
                            ((Golfer) this.player).report(CheatType.SPEED_HACK);
                        }
                        ++this.jumpFP;
                    }
                }
                else if(distDelta > 0.0372247D && !((Golfer) this.player).hasEntityCollisions()) {
                    if(this.jumpFP > 10){
                        this.jumpFP = 0;
                        shouldCancel = true;
                        ((Golfer) this.player).report(CheatType.SPEED_HACK);
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

        if(shouldCancel) {
            this.player.requestTeleport(lastTickX, lastTickY, lastTickZ);
            ci.cancel();
        }*/

        if(!player.hasVehicle()) {
            Vec3d packetMovement = new Vec3d(
                    packet.getX(this.player.getX()) - this.player.getX(),
                    packet.getY(this.player.getY()) - this.player.getY(),
                    packet.getZ(this.player.getZ()) - this.player.getZ()
            );

            boolean onGround = ((Golfer) player).isNearGround();

            if(!this.lLastOnGround && !this.lastOnGround && !onGround) {
                if(packet.isOnGround() && golfConfig.main.yesFall) {
                    ((PlayerMoveC2SPacketAccessor) packet).setOnGround(false);
                    if(player.getVelocity().y <= 0.0D && NO_FALL.equals(this.lastCheat)) {
                        ((Golfer) this.player).report(NO_FALL);
                        player.requestTeleport(this.lastTickX, this.lastTickY, this.lastTickZ);
                        ci.cancel();
                    }
                    else
                        this.lastCheat = NO_FALL;
                }
                if(this.player.isFallFlying() && golfConfig.main.preventElytraHacks) {
                    //System.out.println(packetMovement);
                    if(packetMovement.equals(this.lastMovement) || packetMovement.length() == 0 || this.player.getVelocity().length() == 0) {
                        ++this.elytraFP;
                        if(this.elytraFP > 10 && (ELYTRA_HACK.equals(this.lastCheat) || FLY_HACK.equals(this.lastCheat))) {
                            this.elytraFP = 0;
                            ((Golfer) this.player).report(ELYTRA_HACK);
                            player.requestTeleport(this.lastTickX, this.lastTickY, this.lastTickZ);
                            ci.cancel();
                        }
                        else
                            this.lastCheat = ELYTRA_HACK;
                    }
                    else
                        this.elytraFP = 0;
                }
            }

            if(!this.player.isFallFlying() && checkMove(player, packetMovement, packet.isOnGround())) {
                player.requestTeleport(this.lastTickX, this.lastTickY, this.lastTickZ);
                ci.cancel();
            }
        }
    }


    @Inject(
            method = "onVehicleMove(Lnet/minecraft/network/packet/c2s/play/VehicleMoveC2SPacket;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V",
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    private void checkVehicleMovement(VehicleMoveC2SPacket packet, CallbackInfo ci) {
        Entity vehicle = player.getRootVehicle();
        Vec3d packetMovement = new Vec3d(
                packet.getX() - vehicle.getX(),
                packet.getY() - vehicle.getY(),
                packet.getZ() - vehicle.getZ()
        );

        if(checkMove(vehicle, packetMovement, vehicle.isOnGround())) {
            vehicle.requestTeleport(this.lastTickX, this.lastTickY, this.lastTickZ);
            ci.cancel();
        }

        /*Box bBox = vehicle.getBoundingBox().expand(0, 0.25005D, 0).offset(0, packetMovement.y - 0.25005D, 0);

        Stream<VoxelShape> vs = vehicle.getEntityWorld().getBlockCollisions(vehicle, bBox);
        long blockCollisions = vs.count();
        if(blockCollisions != 0 && ((Golfer) player).hasEntityCollisions()) {
            ((Golfer) player).setEntityCollisions(false);
        }
        ((Golfer) player).setBlockCollisions(blockCollisions != 0);

        if(this.lastMovement == null) {
            this.lastMovement = packetMovement;
            return;
        }
        boolean onGround = ((Golfer) player).isNearGround();
        boolean notLevitating = player.getVelocity().y <= 0.0D;

        BlockPos blockPos = new BlockPos(vehicle.getX(), bBox.minY, vehicle.getZ());
        float slipperiness = this.player.getEntityWorld().getBlockState(blockPos).getBlock().getSlipperiness();
        float friction = !this.lastOnGround && !onGround ? 0.91F : slipperiness * 0.91F;

        double predictedDist = this.lastDist * friction;
        double packetDist = packetMovement.x * packetMovement.x + packetMovement.z * packetMovement.z;
        double distDelta = packetDist - predictedDist;

        if(!this.lLastOnGround && !this.lastOnGround && !onGround) {

            if(distDelta > 0.00750716D) {
                if(this.speedFP > 10) {
                    this.speedFP = 0;
                    shouldCancel = true;
                    ((Golfer) this.player).report(CheatType.SPEED_HACK);
                }
                ++this.speedFP;
                //System.out.println(this.speedFP);
            }

            if(golfConfig.main.noFly && !this.player.abilities.allowFlying && !this.player.isClimbing()) {
                double d = 0.08D;

                if (notLevitating && this.player.hasStatusEffect(StatusEffects.SLOW_FALLING)) {
                    d = 0.01D;
                }

                if(vehicle.isInLava()) {

                }
                else if(vehicle.isTouchingWater()) {

                }
                else {
                    // LivingEntity#travel
                    double predictedDeltaY = (this.lastMovement.y - d) * 0.9800000190734863D;

                    if(Math.abs(predictedDeltaY) >= 0.005D && Math.abs(predictedDeltaY - packetMovement.y) > 0.003) {
                        if(flyAttempts > 2) {
                            flyAttempts = 0;
                            shouldCancel = true;
                            ((Golfer) this.player).report(CheatType.FLY_HACK);
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
                if(distDelta > 0.0372247D && !((Golfer) this.player).hasEntityCollisions()) {
                    if(this.jumpFP > 10){
                        this.jumpFP = 0;
                        shouldCancel = true;
                        ((Golfer) this.player).report(CheatType.SPEED_HACK);
                    }
                    ++this.jumpFP;
                }
                flyAttempts = 0;
            }
        }*/
    }


    private boolean checkMove(Entity entity, Vec3d packetMovement, boolean packetOnGround) {
        boolean shouldCancel = false;

        if(this.lastMovement == null) {
            this.lastMovement = packetMovement;
            return false;
        }

        final Box bBox = entity.getBoundingBox().expand(0, 0.25005D, 0).offset(0, packetMovement.y - 0.25005D, 0);

        Stream<VoxelShape> vs = entity.getEntityWorld().getBlockCollisions(entity, bBox);
        long blockCollisions = vs.count();

        if(blockCollisions != 0 && ((Golfer) player).hasEntityCollisions()) {
            ((Golfer) player).setEntityCollisions(false);
        }
        ((Golfer) player).setBlockCollisions(blockCollisions != 0);

        boolean onGround = ((Golfer) player).isNearGround();
        boolean notLevitating = entity.getVelocity().y <= 0.0D;

        BlockPos blockPos = new BlockPos(entity.getX(), bBox.minY, entity.getZ());
        float slipperiness = entity.getEntityWorld().getBlockState(blockPos).getBlock().getSlipperiness();
        float friction = !this.lastOnGround && !onGround ? 0.91F : slipperiness * 0.91F;

        double predictedDist = this.lastDist * friction;
        double packetDist = packetMovement.x * packetMovement.x + packetMovement.z * packetMovement.z;
        double distDelta = packetDist - predictedDist;

        if(!this.lLastOnGround && !this.lastOnGround && !onGround) {

            if(distDelta > 0.00750716D && !this.player.isCreative()) {
                if(this.speedFP > 10 && (SPEED_HACK.equals(this.lastCheat) || FLY_HACK.equals(this.lastCheat))) {
                    this.speedFP = 0;
                    shouldCancel = true;
                    ((Golfer) this.player).report(SPEED_HACK);
                }
                else
                    this.lastCheat = SPEED_HACK;
                ++this.speedFP;
            }

            if(golfConfig.main.noFly && !this.player.abilities.allowFlying && !notLevitating && (entity instanceof LivingEntity && !((LivingEntity) entity).isClimbing() || entity instanceof BoatEntity)) {
                double d = 0.08D;

                if (entity instanceof LivingEntity && ((LivingEntity) entity).hasStatusEffect(StatusEffects.SLOW_FALLING)) {
                    d = 0.01D;
                }

                if(entity.isInLava()) {

                }
                else if(entity.isTouchingWater()) {

                }
                else {
                    // LivingEntity#travel
                    double predictedDeltaY;

                    if (entity instanceof LivingEntity && ((LivingEntity) entity).hasStatusEffect(StatusEffects.LEVITATION))
                        predictedDeltaY = this.lastMovement.y + (0.05D * (double)(((LivingEntity) entity).getStatusEffect(StatusEffects.LEVITATION).getAmplifier() + 1) - this.lastMovement.y) * 0.2D;
                    else
                        predictedDeltaY = (this.lastMovement.y - d) * 0.9800000190734863D;

                    if(Math.abs(predictedDeltaY) >= 0.005D && Math.abs(predictedDeltaY - packetMovement.y) > 0.003 && (FLY_HACK.equals(this.lastCheat) || SPEED_HACK.equals(this.lastCheat))) {
                        if(flyAttempts > 2) {
                            flyAttempts = 0;
                            shouldCancel = true;
                            ((Golfer) this.player).report(FLY_HACK);
                        }
                        else
                            this.lastCheat = FLY_HACK;
                        ++flyAttempts;
                    }
                    else
                        flyAttempts = 0;
                }
            }
        }
        else if(notLevitating && onGround) {

            double allowedShift;
            if(entity instanceof HorseEntity) {
                allowedShift = 0.102123D;
            }
            else {
                allowedShift = 0.0372247D;
            }

            if(this.lLastOnGround && this.lastOnGround) {
                if(entity instanceof LivingEntity && ((LivingEntity) entity).hasStatusEffect(StatusEffects.SPEED)) {
                    if(packetOnGround && distDelta > (0.35  + ((LivingEntity) entity).getStatusEffect(StatusEffects.SPEED).getAmplifier() * 0.4)) {
                        if(this.jumpFP > 10 && SPEED_HACK.equals(this.lastCheat)){
                            this.jumpFP = 0;
                            shouldCancel = true;
                            ((Golfer) this.player).report(SPEED_HACK);
                        }
                        else
                            this.lastCheat = SPEED_HACK;
                        ++this.jumpFP;
                    }
                    else if(distDelta > (0.05394  + ((LivingEntity) entity).getStatusEffect(StatusEffects.SPEED).getAmplifier() * 0.02)) {
                        if(this.jumpFP > 10 && SPEED_HACK.equals(this.lastCheat)){
                            this.jumpFP = 0;
                            shouldCancel = true;
                            ((Golfer) this.player).report(SPEED_HACK);
                        }
                        else
                            this.lastCheat = SPEED_HACK;
                        ++this.jumpFP;
                    }
                }
                else if(distDelta > allowedShift && !((Golfer) this.player).hasEntityCollisions()) {
                    if(this.jumpFP > 10 && SPEED_HACK.equals(this.lastCheat)){
                        this.jumpFP = 0;
                        shouldCancel = true;
                        ((Golfer) this.player).report(SPEED_HACK);
                    }
                    else
                        this.lastCheat = SPEED_HACK;
                    ++this.jumpFP;
                }
                flyAttempts = 0;
            }
            else if (!packetOnGround) {
                this.jumpFP = 0;
            }
        }

        this.lastDist = packetDist;
        this.lastMovement = packetMovement;
        this.lLastOnGround = this.lastOnGround;
        this.lastOnGround = onGround;

        return shouldCancel;
    }
}
