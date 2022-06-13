package org.samo_lego.golfiv.event.combat;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;

public class CombatModule {
    public static void registerEvents() {
        var angleCheck = new AngleCheck();
        var reachCheck = new ReachCheck();
        var wallHitCheck = new WallHitCheck();
        var invHitCheck = new InventoryHitCheck();

        UseEntityCallback.EVENT.register(angleCheck);
        AttackEntityCallback.EVENT.register(angleCheck);

        UseEntityCallback.EVENT.register(reachCheck);
        AttackEntityCallback.EVENT.register(reachCheck);

        UseEntityCallback.EVENT.register(wallHitCheck);
        AttackEntityCallback.EVENT.register(wallHitCheck);

        UseEntityCallback.EVENT.register(invHitCheck);
        AttackEntityCallback.EVENT.register(invHitCheck);
    }
}
