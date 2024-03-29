package org.samo_lego.golfiv.mixin.movement;

import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import org.samo_lego.golfiv.casts.Golfer;
import org.samo_lego.golfiv.mixin.accessors.PlayerMoveC2SPacketAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static org.samo_lego.golfiv.GolfIV.golfConfig;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandler_OnGroundCheck {

    @Shadow public ServerPlayerEntity player;

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
        if (golfConfig.movement.patchNoFall && packet.isOnGround()) {
            Entity bottomEntity = player.getRootVehicle();
            if (bottomEntity == null) {
                bottomEntity = player;
            }
            final Box bBox = bottomEntity.getBoundingBox().expand(0, 0.25005D, 0).offset(0, packet.getY(player.getY()) - player.getY() - 0.25005D, 0);

            Iterable<VoxelShape> collidingBlocks = player.getEntityWorld().getBlockCollisions(bottomEntity, bBox);
            boolean blockCollisions = collidingBlocks.iterator().hasNext();

            if (blockCollisions) {
                // Preferring block collisions over entity ones
                ((Golfer) player).setEntityCollisions(false);
                ((Golfer) player).setBlockCollisions(true);
            } else {
                Entity finalBottomEntity = bottomEntity;
                List<Entity> collidingEntities = player.getEntityWorld().getOtherEntities(bottomEntity, bBox, entity -> !finalBottomEntity.equals(entity));

                ((Golfer) player).setEntityCollisions(!collidingEntities.isEmpty());
                ((Golfer) player).setBlockCollisions(false);
            }

            if(!((Golfer) player).isNearGround()) {
                // Player isn't on ground but client packet says it is
                ((PlayerMoveC2SPacketAccessor) packet).setOnGround(false);
            }
        }
    }
}
