package org.samo_lego.golfiv.event.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.EntityHitResult;

import static org.samo_lego.golfiv.GolfIV.golfConfig;

public class ReachCheck implements EntityInteractPacketCallback {
    public ReachCheck() {
    }

    @Override
    public ActionResult onEntityInteractPacket(PlayerEntity player, Entity victim, double maxDistanceSquared) {
        if(golfConfig.combat.checkHitDistance) {
            EntityHitResult entityHit = new EntityHitResult(victim);
            double victimDistanceSquared = entityHit.squaredDistanceTo(player);

            if(golfConfig.combat.checkHitDistance && !player.isCreative() && victimDistanceSquared > 22) {
                return ActionResult.FAIL;
            }
        }
        return ActionResult.PASS;
    }
}
