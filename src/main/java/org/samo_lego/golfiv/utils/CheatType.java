package org.samo_lego.golfiv.utils;

/**
 * Enum of cheat types.
 * Some are grouped together since they use
 * similar checks.
 */
public enum CheatType {
    FLY_HACK("fly"),
    SPEED_HACK("speed"),
    ELYTRA_HACK("elytra"),
    NO_FALL("no fall"),
    JESUS("Jesus"),
    STEP("step"),
    ILLEGAL_ACTIONS("inventory walk"),
    KILLAURA("killaura"),
    NO_HAND_SWING("no hand swing"),
    REACH("reach"),
    HIT_THROUGH_WALLS("hit through walls"),
    TIMER("timer"),
    NBT_ITEMS("nbt items"),
    SUSPICIOUS_CREATIVE("suspicious creative");

    private final String cheatString;

    CheatType(String cheat) {
        this.cheatString = cheat;
    }

    public String getCheat() {
        return this.cheatString;
    }
}
