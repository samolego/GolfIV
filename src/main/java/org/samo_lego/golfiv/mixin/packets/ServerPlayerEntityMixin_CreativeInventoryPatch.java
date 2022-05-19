package org.samo_lego.golfiv.mixin.packets;// Created 2022-08-01T15:18:06

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.samo_lego.golfiv.GolfIV;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Injections to fix Creative in the case of a change in game mode.
 *
 * @author KJP12
 **/
@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin_CreativeInventoryPatch extends PlayerEntity {
    public ServerPlayerEntityMixin_CreativeInventoryPatch(World world, BlockPos pos, float yaw, GameProfile profile, @Nullable PlayerPublicKey publicKey) {
        super(world, pos, yaw, profile, publicKey);
    }

    /**
     * Forcefully update the screen on game mode change to Creative.
     *
     * @param gameMode The new game mode.
     * @param cir      Ignored; normally for returning something else.
     * @reason Ensure that Creative doesn't accidentally nuke its own inventory.
     * @author KJP12
     */
    @Inject(method = "changeGameMode", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V"))
    private void onChangeGameMode(GameMode gameMode, CallbackInfoReturnable<Boolean> cir) {
        if (GolfIV.golfConfig.packet.patchItemKickExploit && gameMode.isCreative()) {
            currentScreenHandler.syncState();
        }
    }
}
