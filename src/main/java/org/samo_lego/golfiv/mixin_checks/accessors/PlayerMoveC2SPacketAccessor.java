package org.samo_lego.golfiv.mixin_checks.accessors;


import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accessor for the movement packet.
 */
@Mixin(PlayerMoveC2SPacket.class)
public interface PlayerMoveC2SPacketAccessor {

    /**
     * Allows to change packet onGround value.
     * @param onGround whether player is really on ground.
     */
    @Accessor("onGround")
    void setOnGround(boolean onGround);
}
