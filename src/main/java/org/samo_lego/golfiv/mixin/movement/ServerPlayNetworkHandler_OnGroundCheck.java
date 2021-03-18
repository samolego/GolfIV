package org.samo_lego.golfiv.mixin.movement;

import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import org.samo_lego.golfiv.casts.Golfer;
import org.samo_lego.golfiv.casts.NetworkHandlerData;
import org.samo_lego.golfiv.mixin.accessors.PlayerMoveC2SPacketAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.stream.Stream;

import static org.samo_lego.golfiv.GolfIV.golfConfig;

/**
 * This has a different priority since the data it provides
 * is used in other checks as well.
 */
@Mixin(value = ServerPlayNetworkHandler.class, priority = 800)
public class ServerPlayNetworkHandler_OnGroundCheck {

    @Shadow public ServerPlayerEntity player;

    @Unique
    private final NetworkHandlerData data = (NetworkHandlerData) this;

    /**
     * Checks the real onGround value of the movement packet.
     *
     * @param packet
     * @param ci
     */
    @Inject(
            method = "onPlayerMove(Lnet/minecraft/network/packet/c2s/play/PlayerMoveC2SPacket;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;hasVehicle()Z"
            )
    )
    private void checkOnGround(PlayerMoveC2SPacket packet, CallbackInfo ci) {
        if(data.getPacketMovement() != null) {
            Entity bottomEntity = player.getRootVehicle();
            if(bottomEntity == null) {
                bottomEntity = player;
            }
            final Box bBox = bottomEntity.getBoundingBox().expand(0, 0.25005D, 0).offset(0, data.getPacketMovement().y - 0.25005D, 0);

            Stream<VoxelShape> collidingBlocks = player.getEntityWorld().getBlockCollisions(bottomEntity, bBox);
            long blockCollisions = collidingBlocks.count();

            if(blockCollisions != 0) {
                // Preferring block collisions over entity ones
                ((Golfer) player).setEntityCollisions(false);
                ((Golfer) player).setBlockCollisions(true);
                ((Golfer) player).setNearFluid(false);
            }
            else {
                Entity finalBottomEntity = bottomEntity;
                Stream<VoxelShape> collidingEntities = player.getEntityWorld().getEntityCollisions(bottomEntity, bBox, entity -> !finalBottomEntity.equals(entity));
                long entityCollisions = collidingEntities.count();

                ((Golfer) player).setEntityCollisions(entityCollisions != 0);
                ((Golfer) player).setBlockCollisions(false);
                ((Golfer) player).setNearFluid(entityCollisions == 0 && player.getEntityWorld().containsFluid(bBox));
            }

            if(!data.wasLLastOnGround() && !data.wasLastOnGround() && !((Golfer) player).isNearGround() && player.getVelocity().y <= 0.0D && packet.isOnGround() && golfConfig.movement.yesFall) {
                // Player hasn't been on ground for 3 move packets but client says it is
                ((PlayerMoveC2SPacketAccessor) packet).setOnGround(false);
            }
        }
    }
}
