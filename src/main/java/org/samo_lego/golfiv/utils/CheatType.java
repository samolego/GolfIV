package org.samo_lego.golfiv.utils;

public enum CheatType {
    FLY_HACK("fly"),
    SPEED_HACK("speed"),
    ELYTRA_HACK("elytra"),
    NO_FALL("no fall / Jesus"),
    TIMER("timer"),
    SUSPICIOUS_CREATIVE("suspicious creative");

    private final String cheatString;

    CheatType(String cheat) {
        this.cheatString = cheat;
    }

    public String getCheat() {
        return this.cheatString;
    }
}
