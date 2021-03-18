package org.samo_lego.golfiv.casts;

import net.minecraft.util.math.Vec3d;

public interface NetworkHandlerData {

    boolean wasLastOnGround();

    boolean wasLLastOnGround();

    Vec3d getPacketMovement();
}
