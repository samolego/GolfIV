package org.samo_lego.golfiv.mixin_checks.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerSpawnS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.samo_lego.golfiv.casts.Golfer;
import org.samo_lego.golfiv.mixin_checks.accessors.PlayerInteractEntityC2SPacketAccessor;
import org.samo_lego.golfiv.utils.FakeVictim;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static net.minecraft.network.packet.s2c.play.PlayerListS2CPacket.Action.ADD_PLAYER;
import static net.minecraft.network.packet.s2c.play.PlayerListS2CPacket.Action.REMOVE_PLAYER;
import static org.samo_lego.golfiv.GolfIV.golfConfig;
import static org.samo_lego.golfiv.utils.CheatType.KILLAURA;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin_killauraCheck {

    @Shadow public ServerPlayerEntity player;

    @Shadow private int ticks;
    @Unique
    private FakeVictim fakeVictim;
    @Unique
    private boolean fakeAttacked;

    @Inject(
            method = "onPlayerInteractEntity(Lnet/minecraft/network/packet/c2s/play/PlayerInteractEntityC2SPacket;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;setSneaking(Z)V"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true
    )
    private void checkHitEntity(PlayerInteractEntityC2SPacket packet, CallbackInfo ci, ServerWorld serverWorld, Entity target) {
        if(golfConfig.main.checkKillaura && fakeVictim != null) {
            this.fakeAttacked = ((PlayerInteractEntityC2SPacketAccessor) packet).getEntityId() == this.fakeVictim.getEntityId();
            if(fakeAttacked)
                ((Golfer) player).report(KILLAURA, 1);
            this.clearFakeVictim();
        }
    }

    @Inject(
            method = "onPlayerInteractEntity(Lnet/minecraft/network/packet/c2s/play/PlayerInteractEntityC2SPacket;)V",
            at = @At("TAIL"),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true
    )
    private void testWithFakeVictim(PlayerInteractEntityC2SPacket packet, CallbackInfo ci, Entity target) {
        if(golfConfig.main.checkKillaura && fakeVictim == null && target instanceof LivingEntity && !target.isAlive()) {
            this.fakeVictim = FakeVictim.summonFake(player);
            player.networkHandler.sendPacket(new PlayerListS2CPacket(ADD_PLAYER, fakeVictim));
            player.networkHandler.sendPacket(new PlayerSpawnS2CPacket(fakeVictim));
        }
    }

    @Unique
    private void clearFakeVictim() {
        player.networkHandler.sendPacket(new PlayerListS2CPacket(REMOVE_PLAYER, fakeVictim));
        player.onStoppedTracking(this.fakeVictim);
        this.fakeVictim = null;
    }

    @Inject(method = "tick()V", at = @At("HEAD"))
    private void preTick(CallbackInfo ci) {
        if(fakeVictim != null && player.getAttackCooldownProgress(0.5F) == 1 && this.ticks % 4 == 0) {
            this.clearFakeVictim();
        }
    }

    @Inject(
        method = "onPlayerMove(Lnet/minecraft/network/packet/c2s/play/PlayerMoveC2SPacket;)V",
        at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/server/network/ServerPlayerEntity;hasVehicle()Z"
        )
    )
    private void updateFakePosition(PlayerMoveC2SPacket packet, CallbackInfo ci) {
        if(fakeVictim != null) {
            Vec3d movement = new Vec3d(
                    packet.getX(this.player.getX()),
                    packet.getY(this.player.getY()),
                    packet.getZ(this.player.getZ())
            );
            Vec2f rotations = new Vec2f(
                    packet.getYaw(this.player.yaw),
                    packet.getPitch(this.player.pitch)
            );

            this.fakeVictim.rotateAroundPlayer(movement, rotations);
            player.networkHandler.sendPacket(new EntityPositionS2CPacket(fakeVictim));
        }
    }
}
