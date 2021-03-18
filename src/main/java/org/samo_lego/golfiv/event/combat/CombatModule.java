package org.samo_lego.golfiv.event.combat;

public class CombatModule {
    public static void registerEvents() {
        EntityInteractPacketCallback.EVENT.register(new AngleCheck());
        EntityInteractPacketCallback.EVENT.register(new ReachCheck());
        EntityInteractPacketCallback.EVENT.register(new WallHitCheck());
    }
}
