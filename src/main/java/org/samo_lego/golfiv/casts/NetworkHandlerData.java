package org.samo_lego.golfiv.casts;

import net.minecraft.util.math.Vec3d;

public interface NetworkHandlerData {

    void setLastOnGround(boolean lastOnGround);
    boolean wasLastOnGround();

    void setLLastOnGround(boolean lLastOnGround);
    boolean wasLLastOnGround();

    void setLastMovement(Vec3d lastMovement);
    Vec3d getLastMovement();

    Vec3d getPacketMovement();

    void setLastDist(double lastDist);
    double getLastDist();
}
