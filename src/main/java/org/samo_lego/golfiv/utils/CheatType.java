package org.samo_lego.golfiv.utils;

public enum CheatType {
    FLY_HACK("fly"),
    SPEED_HACK("speed"),
    NO_FALL("no fall / Jesus"),
    SUSPICIOUS_CREATIVE("suspicious creative");

    private String cheatString;

    CheatType(String cheat) {
        this.cheatString = cheat;
    }

    public String getCheat() {
        return this.cheatString;
    }
}
