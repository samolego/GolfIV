package org.samo_lego.golfiv.event.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import org.samo_lego.golfiv.casts.Golfer;

import static org.samo_lego.golfiv.GolfIV.golfConfig;

public class InventoryHitCheck implements EntityInteractPacketCallback {

    public InventoryHitCheck() {
    }

    /**
     * Checks whether player is trying to hit an entity
     * while having open inventory.
     *
     * @param player player trying to interact with entity.
     * @param victim entity player is trying to interact with.
     * @param maxDistanceSquared maximal allowed distance for interaction, squared.
     * @return {@link ActionResult#FAIL} if player shouldn't be able to hit the victim, otherwise {@link ActionResult#PASS}
     */
    @Override
    public ActionResult onEntityInteractPacket(PlayerEntity player, Entity victim, double maxDistanceSquared) {
        return golfConfig.main.checkIllegalActions && ((Golfer) player).hasOpenGui() ? ActionResult.FAIL : ActionResult.PASS;
    }
}
