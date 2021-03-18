package org.samo_lego.golfiv.event.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;

import static org.samo_lego.golfiv.GolfIV.golfConfig;

public class AngleCheck implements EntityInteractPacketCallback {
    public AngleCheck() {
    }

    /**
     * Checks the angle at which player is hitting the entity.
     *
     * @param player player trying to interact with entity.
     * @param victim entity player is trying to interact with.
     * @param maxDistanceSquared maximal allowed distance for interaction, squared.
     * @return {@link ActionResult#FAIL} if player shouldn't be able to hit the victim, otherwise {@link ActionResult#PASS}
     */
    @Override
    public ActionResult onEntityInteractPacket(PlayerEntity player, Entity victim, double maxDistanceSquared) {
        if(golfConfig.combat.checkHitAngle) {
            EntityHitResult entityHit = new EntityHitResult(victim);
            double victimDistanceSquared = entityHit.squaredDistanceTo(player);
            double victimDistance = Math.sqrt(victimDistanceSquared);

            // Angle check
            int xOffset = player.getHorizontalFacing().getOffsetX();
            int zOffset = player.getHorizontalFacing().getOffsetZ();
            Box bBox = victim.getBoundingBox();

            // Checking if victim is behind player
            if(xOffset * victim.getX() + bBox.getXLength() / 2 - xOffset * player.getX() < 0 || zOffset * victim.getZ() + bBox.getZLength() / 2 - zOffset * player.getZ() < 0) {
                // "Dumb" check
                return ActionResult.FAIL;
            }
            double deltaX = victim.getX() - player.getX();
            double deltaZ = victim.getZ() - player.getZ();
            double beta = Math.atan2(deltaZ, deltaX) - Math.PI / 2;

            double phi = beta - Math.toRadians(player.yaw);
            //todo can be improved?
            double allowedAttackSpace = Math.sqrt(bBox.getXLength() * bBox.getXLength() + bBox.getZLength() * bBox.getZLength());

            if(Math.abs(victimDistance * Math.sin(phi)) > allowedAttackSpace / 2 + 0.2D) {
                // Fine check
                return ActionResult.FAIL;
            }
        }

        return ActionResult.PASS;
    }
}
