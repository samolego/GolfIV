package org.samo_lego.golfiv.event.S2CPacket;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Saddleable;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.samo_lego.golfiv.mixin.accessors.EntityTrackerUpdateS2CPacketAccessor;
import org.samo_lego.golfiv.mixin.accessors.ItemEntityAccessor;
import org.samo_lego.golfiv.mixin.accessors.LivingEntityAccessor;
import org.samo_lego.golfiv.mixin.accessors.PlayerEntityAccessor;

import static org.samo_lego.golfiv.GolfIV.golfConfig;
import static org.samo_lego.golfiv.casts.ItemStackChecker.fakeStack;

public class EntityTrackerDataPatch implements S2CPacketCallback {
    private static TrackedData<Float> LIVING_ENTITY_HEALTH = LivingEntityAccessor.getHealth();
    private static TrackedData<Float> PLAYER_ENTITY_ABSORPTION = PlayerEntityAccessor.getAbsorption();
    private static TrackedData<ItemStack> ITEM_ENTITY_STACK = ItemEntityAccessor.getSTACK();

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
                p.getTrackedValues().removeIf(trackedValue -> trackedValue.getData().equals(LIVING_ENTITY_HEALTH));
                p.getTrackedValues().removeIf(trackedValue -> trackedValue.getData().equals(PLAYER_ENTITY_ABSORPTION));
                if(entity instanceof IronGolemEntity || entity instanceof WitherEntity) {
                    // Reinjects the health data aligned to quarters.
                    LivingEntity ironGolem = (LivingEntity) entity;

                    Float newHealth = MathHelper.floor((ironGolem.getHealth() - 1F) / 25F) * 25F + 1F;

                    DataTracker.Entry<Float> fakeEntry = new DataTracker.Entry<>(LIVING_ENTITY_HEALTH, newHealth);
                    p.getTrackedValues().add(fakeEntry);
                }
            } else if(golfConfig.packet.removeDroppedItemInfo && entity instanceof ItemEntity) {
                boolean removed = p.getTrackedValues().removeIf(entry -> entry.getData().equals(ITEM_ENTITY_STACK)); // Original item
                if(removed) {
                    ItemStack original = ((ItemEntity) entity).getStack();

                    DataTracker.Entry<ItemStack> fakeEntry = new DataTracker.Entry<>(ITEM_ENTITY_STACK, fakeStack(original, false));
                    p.getTrackedValues().add(fakeEntry);
                }
            }
        }
    }
}
