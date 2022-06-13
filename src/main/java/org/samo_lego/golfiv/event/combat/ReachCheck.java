package org.samo_lego.golfiv.event.combat;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import static org.samo_lego.golfiv.GolfIV.golfConfig;

public class ReachCheck implements UseEntityCallback, AttackEntityCallback {
    public ReachCheck() {
    }

    /**
     * Checks if player is trying to interact with entity
     * while being too far away.
     *
     * @param player player trying to interact with entity.
     * @param victim entity player is trying to interact with.
     * @return {@link ActionResult#FAIL} if player shouldn't be able to hit the victim, otherwise {@link ActionResult#PASS}
     */
    @Override
    public ActionResult interact(PlayerEntity player, World world, Hand hand, Entity victim, @Nullable EntityHitResult hitResult) {
        if (golfConfig.combat.checkHitDistance) {
            EntityHitResult entityHit = new EntityHitResult(victim);
            double victimDistanceSquared = entityHit.squaredDistanceTo(player);

            if (golfConfig.combat.checkHitDistance && !player.isCreative() && victimDistanceSquared > 22) {
                return ActionResult.FAIL;
            }
        }
        return ActionResult.PASS;

    }
}
