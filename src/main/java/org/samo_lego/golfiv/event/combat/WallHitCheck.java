package org.samo_lego.golfiv.event.combat;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import static org.samo_lego.golfiv.GolfIV.golfConfig;

public class WallHitCheck implements AttackEntityCallback, UseEntityCallback {
    public WallHitCheck() {
    }

    /**
     * Checks if there's a block between a player and entity
     * the player is trying to interact with.
     *
     * @param player player trying to interact with entity.
     * @param victim entity player is trying to interact with.
     * @return {@link ActionResult#FAIL} if player shouldn't be able to hit the victim, otherwise {@link ActionResult#PASS}
     */
    @Override
    public ActionResult interact(PlayerEntity player, World world, Hand hand, Entity victim, @Nullable EntityHitResult hitResult) {
        if (golfConfig.combat.preventWallHit) {
            EntityHitResult entityHit = new EntityHitResult(victim);
            double victimDistanceSquared = entityHit.squaredDistanceTo(player);
            double victimDistance = Math.sqrt(victimDistanceSquared);

            // Through-wall hit check
            BlockHitResult blockHit = (BlockHitResult) player.raycast(Math.sqrt(64.0), 0, false);

            if (Math.sqrt(blockHit.squaredDistanceTo(player)) + 0.5D < victimDistance) {
                return ActionResult.FAIL;
            }
        }
        return ActionResult.PASS;

    }
}
