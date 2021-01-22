package org.samo_lego.golfiv.mixin_checks.movement;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import org.samo_lego.golfiv.casts.Golfer;
import org.samo_lego.golfiv.casts.NetworkHandlerData;
import org.samo_lego.golfiv.mixin_checks.accessors.PlayerMoveC2SPacketAccessor;
import org.samo_lego.golfiv.utils.CheatType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.stream.Stream;

import static org.samo_lego.golfiv.GolfIV.golfConfig;
import static org.samo_lego.golfiv.utils.CheatType.SAFE_WALK;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin_SafeWalkCheck {

    @Shadow public ServerPlayerEntity player;
    @Unique
    private final NetworkHandlerData data = (NetworkHandlerData) this;
    @Unique
    private byte edgeWalk;
    @Unique
    private boolean wasAir;
    @Unique
    private boolean wasLastAir;

    /**
     * Checks X and Z movement on the edge of the block.
     *
     * @param packet
     * @param ci
     */
    @Inject(
            method = "onPlayerMove(Lnet/minecraft/network/packet/c2s/play/PlayerMoveC2SPacket;)V",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lnet/minecraft/network/packet/c2s/play/PlayerMoveC2SPacket;getX(D)D"
            ),
            cancellable = true
    )
    private void checkSafeWalk(PlayerMoveC2SPacket packet, CallbackInfo ci) {
        if(golfConfig.movement.hazardousWalk && !player.isFallFlying() && !player.isSneaking() && ((PlayerMoveC2SPacketAccessor) packet).changesPosition() && !player.isCreative()) {
            Vec3d packetMovement = data.getPacketMovement();

            if(packetMovement.getX() == 0 ^ packetMovement.getZ() == 0) {
                BlockPos pos = player.getBlockPos().offset(Direction.Axis.Y, -1);
                boolean air = player.getServerWorld().getBlockState(pos).isAir();

                Box box = player.getBoundingBox().expand(0.01D, 0.0D, 0.01D);
                Stream<VoxelShape> collidingBlocks = player.getEntityWorld().getBlockCollisions(player, box);
                long col = collidingBlocks.count();

                if(((Golfer) player).isNearGround() && air && this.wasAir && this.wasLastAir && col == 0 && ++this.edgeWalk > 4) {
                    ((Golfer) this.player).report(SAFE_WALK, golfConfig.sus.safeWalk);

                    this.player.requestTeleport(player.getX(), player.getY(), player.getZ());
                    ci.cancel();
                }
                this.wasLastAir = this.wasAir;
                this.wasAir = air;
            }
            else
                this.edgeWalk = 0;
        }
    }
}
