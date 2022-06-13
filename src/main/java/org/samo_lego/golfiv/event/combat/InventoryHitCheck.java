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
import org.samo_lego.golfiv.casts.Golfer;

import static org.samo_lego.golfiv.GolfIV.golfConfig;

public class InventoryHitCheck implements UseEntityCallback, AttackEntityCallback {

    public InventoryHitCheck() {
    }

    /**
     * Checks whether player is trying to hit an entity
     * while having open inventory.
     *
     * @param player player trying to interact with entity.
     * @return {@link ActionResult#FAIL} if player shouldn't be able to hit the victim, otherwise {@link ActionResult#PASS}
     */
    @Override
    public ActionResult interact(PlayerEntity player, World world, Hand hand, Entity entity, @Nullable EntityHitResult hitResult) {
        return golfConfig.main.checkInventoryActions && ((Golfer) player).hasOpenGui() ? ActionResult.FAIL : ActionResult.PASS;
    }
}
