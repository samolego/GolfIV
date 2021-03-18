package org.samo_lego.golfiv.event.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;

import static org.samo_lego.golfiv.GolfIV.golfConfig;

public class WallHitCheck implements EntityInteractPacketCallback {
    public WallHitCheck() {
    }

    @Override
    public ActionResult onEntityInteractPacket(PlayerEntity player, Entity victim, double maxDistanceSquared) {
        if(golfConfig.combat.checkWallHit) {
            EntityHitResult entityHit = new EntityHitResult(victim);
            double victimDistanceSquared = entityHit.squaredDistanceTo(player);
            double victimDistance = Math.sqrt(victimDistanceSquared);

            // Through-wall hit check
            BlockHitResult blockHit = (BlockHitResult) player.raycast(Math.sqrt(maxDistanceSquared), 0, false);

            if(Math.sqrt(blockHit.squaredDistanceTo(player)) + 0.5D < victimDistance) {
                return ActionResult.FAIL;
            }
        }
        return ActionResult.PASS;
    }
}
