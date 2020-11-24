package org.samo_lego.golfiv.utils.casts;

import org.samo_lego.golfiv.utils.CheatType;

public interface Golfer {
    /*void setLies(int newLies);
    int getLies();*/

    boolean isNearGround();

    void setBlockCollisions(boolean blockCollision);
    boolean hasBlockCollisions();

    void setEntityCollisions(boolean entityCollision);
    boolean hasEntityCollisions();

    void report(CheatType cheatType);
}
