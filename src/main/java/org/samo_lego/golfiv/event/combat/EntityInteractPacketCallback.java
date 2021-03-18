package org.samo_lego.golfiv.event.combat;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;

public interface EntityInteractPacketCallback {

    Event<EntityInteractPacketCallback> EVENT = EventFactory.createArrayBacked(EntityInteractPacketCallback.class,
        (listeners) -> (player, entity, maxDistanceSquared) -> {
            for (EntityInteractPacketCallback listener : listeners) {
                ActionResult result = listener.onEntityInteractPacket(player, entity, maxDistanceSquared);

                if(result != ActionResult.PASS)  {
                    return result;
                }
            }
            return ActionResult.PASS;
     });

    ActionResult onEntityInteractPacket(PlayerEntity player, Entity victim, double maxDistanceSquared);
}
