package org.samo_lego.golfiv.event.S2CPacket;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Saddleable;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.samo_lego.golfiv.mixin.accessors.EntityTrackerUpdateS2CPacketAccessor;
import org.samo_lego.golfiv.mixin.accessors.ItemEntityAccessor;

import static org.samo_lego.golfiv.GolfIV.golfConfig;
import static org.samo_lego.golfiv.utils.FakeItemConstructor.fakeStack;

public class EntityTrackerDataPatch implements S2CPacketCallback {

    public EntityTrackerDataPatch() {
    }

    /**
     * Removes entity tracker info from packet.
     *
     * @see <a href="https://wiki.vg/Entity_metadata">Entity Tracker info</a>
     *
     * @param packet packet being sent
     * @param player player getting the packet
     * @param server Minecraft Server
     */
    @Override
    public void preSendPacket(Packet<?> packet, ServerPlayerEntity player, MinecraftServer server) {
        if(packet instanceof EntityTrackerUpdateS2CPacket) {
            EntityTrackerUpdateS2CPacketAccessor p = ((EntityTrackerUpdateS2CPacketAccessor) packet);
            Entity entity = player.getServerWorld().getEntityById(p.getId());

            if(golfConfig.packet.removeHealthTags && entity instanceof LivingEntity && entity.isAlive() && !(entity instanceof Saddleable)) {
                p.getTrackedValues().removeIf(trackedValue -> trackedValue.getData().getId() == 8); // Health
                p.getTrackedValues().removeIf(trackedValue -> trackedValue.getData().getId() == 12); // Absorption health
            } else if(golfConfig.packet.removeDroppedItemInfo && entity instanceof ItemEntity) {
                boolean removed = p.getTrackedValues().removeIf(entry -> entry.getData().getId() == 7); // Original item
                if(removed) {
                    ItemStack original = ((ItemEntity) entity).getStack();

                    DataTracker.Entry<ItemStack> fakeEntry = new DataTracker.Entry<>(ItemEntityAccessor.getSTACK(), fakeStack(original, original.getCount()));
                    p.getTrackedValues().add(fakeEntry);
                }
            }
        }
    }
}
