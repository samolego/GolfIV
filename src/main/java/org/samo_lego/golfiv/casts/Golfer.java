package org.samo_lego.golfiv.casts;

import net.minecraft.nbt.ListTag;
import org.samo_lego.golfiv.utils.CheatType;

/**
 * Additional methods for player data tracking / logging.
 */
public interface Golfer {
    /*void setLies(int newLies);
    int getLies();*/

    /**
     * Real onGround value, which isn't affected
     * by the client packet.
     *
     * @return true if player is near ground (0.5001 block tolerance), otherwise false.
     */
    boolean isNearGround();

    /**
     * Sets whether player has block collisions.
     *
     * @param blockCollisions whether player has block collisions.
     */
    void setBlockCollisions(boolean blockCollisions);

    /**
     * Sets whether player has entity collisions (e. g. boat collisions).
     *
     * @param entityCollisions whether player has entity collisions.
     */
    void setEntityCollisions(boolean entityCollisions);

    /**
     * Tells whether player is near fluid (above).
     *
     * @return true if player is near fluid collisions, otherwise false.
     */
    boolean isNearFluid();

    /**
     * Sets the nearFluid status.
     *
     * @param nearFluid if player is near (above) fluid
     */
    void setNearFluid(boolean nearFluid);

    /**
     * Reports player for cheating.
     *
     * @param cheatType type of the cheat player has used.
     */
    void report(CheatType cheatType, int susValue);

    /**
     * Sets whether player has opened GUI.
     * Doesn't catch opening their own inventory.
     *
     * @param openGui whether player has opened the GUI.
     */
    void setOpenGui(boolean openGui);

    /**
     * Tells whether player has open GUI.
     * Doesn't catch their own inventory being open.
     *
     * @return true if player has open GUI, otherwise false
     */
    boolean hasOpenGui();

    /**
     * Gets the number of ticks that the player
     * has had an open GUI while in a nether portal
     *
     * @return number of ticks, greater than or equal to 0
     */
    int getGuiOpenInPortalTicks();

    /**
     * Sets the number of ticks that the player
     * has had an open GUI while in a nether portal
     *
     * @param ticks
     */
    void setGuiOpenInPortalTicks(int ticks);

    /**
     * Gets the suspicion value for the player.
     *
     * @return suspicion value, higher than 0
     */
    int getSusLevel();

    /**
     * Sets suspicion value for the player.
     */
    void setSusLevel(int newSusLevel);

    /**
     * Clears cheat log from the player.
     */
    void clearCheatLog();

    /**
     * Gets cheat log for the player.
     */
    ListTag getCheatLog();

    int getHitAccuracy();

    void setHitAccuracy(int entityHits, int handSwings);
    void setHitAccuracy(int accuracy);

    /**
     * Gets the number of times player has been kicked.
     *
     * @return player's number of kicks.
     */
    short getKicks();
}
