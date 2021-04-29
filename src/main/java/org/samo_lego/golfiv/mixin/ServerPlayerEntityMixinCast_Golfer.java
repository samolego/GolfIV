package org.samo_lego.golfiv.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.samo_lego.golfiv.casts.Golfer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Additional methods and fields for PlayerEntities.
 */
@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixinCast_Golfer implements Golfer {

    @Shadow @Final public MinecraftServer server;

    @Unique
    private boolean blockCollisions, entityCollisions, hasOpenScreen;

    @Unique
    private int guiOpenInPortalTicks;

    @Unique
    private boolean nearFluid;

    /**
     * Real onGround value, which isn't affected
     * by the client packet.
     *
     * @return true if player is near ground (0.5001 block tolerance), otherwise false.
     */
    @Override
    public boolean isNearGround() {
        return blockCollisions || entityCollisions;
    }

    /**
     * Sets whether player has block collisions.
     *
     * @param blockCollisions whether player has block collisions.
     */
    @Override
    public void setBlockCollisions(boolean blockCollisions) {
        this.blockCollisions = blockCollisions;
    }

    /**
     * Sets whether player has entity collisions (e. g. boat collisions).
     *
     * @param entityCollisions whether player has entity collisions.
     */
    @Override
    public void setEntityCollisions(boolean entityCollisions) {
        this.entityCollisions = entityCollisions;
    }

    /**
     * Tells whether player is near fluid (above).
     *
     * @return true if player is near fluid collisions, otherwise false.
     */
    @Override
    public boolean isNearFluid() {
        return this.nearFluid;
    }

    /**
     * Sets the nearFluid status.
     *
     * @param nearFluid if player is near (above) fluid
     */
    @Override
    public void setNearFluid(boolean nearFluid) {
        this.nearFluid = nearFluid;
    }

    /**
     * Sets whether player has opened GUI.
     * Doesn't catch opening their own inventory.
     *
     * @param openGui whether player has opened the GUI.
     */
    @Override
    public void setOpenGui(boolean openGui) {
        this.hasOpenScreen = openGui;
    }

    /**
     * Tells whether player has open GUI.
     * Doesn't catch their own inventory being open.
     *
     * @return true if player has open GUI, otherwise false
     */
    @Override
    public boolean hasOpenGui() {
        return this.hasOpenScreen;
    }

    /**
     * Gets the number of ticks that the player
     * has had an open GUI while in a nether portal
     *
     * @return number of ticks, greater than or equal to 0
     */
    @Override
    public int getGuiOpenInPortalTicks() {
        return this.guiOpenInPortalTicks;
    }

    /**
     * Sets the number of ticks that the player
     * has had an open GUI while in a nether portal
     *
     * @param ticks
     */
    @Override
    public void setGuiOpenInPortalTicks(int ticks) {
        this.guiOpenInPortalTicks = ticks;
    }
}
