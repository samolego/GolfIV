package org.samo_lego.golfiv.mixin.packets;// Created 2021-30-06T12:17:18

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Saddleable;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.samo_lego.golfiv.mixin.accessors.DataTrackerAccessor;
import org.samo_lego.golfiv.mixin.accessors.ItemEntityAccessor;
import org.samo_lego.golfiv.mixin.accessors.LivingEntityAccessor;
import org.samo_lego.golfiv.mixin.accessors.PlayerEntityAccessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static org.samo_lego.golfiv.GolfIV.golfConfig;
import static org.samo_lego.golfiv.casts.ItemStackChecker.fakeStack;

/**
 * Removes health, absorption and stack NBT from various entities.
 *
 * @author KJP12
 * @see <a href="https://wiki.vg/Entity_metadata">Entity Tracker info</a>
 **/
@Mixin(EntityTrackerUpdateS2CPacket.class)
public class EntityTrackerUpdateS2CPacketMixin_DataPatch {
    private static final TrackedData<Float> LIVING_ENTITY_HEALTH = LivingEntityAccessor.getHealth();
    private static final TrackedData<Float> PLAYER_ENTITY_ABSORPTION = PlayerEntityAccessor.getAbsorption();
    private static final TrackedData<ItemStack> ITEM_ENTITY_STACK = ItemEntityAccessor.getSTACK();

    @Shadow
    @Final
    @Nullable
    private List<DataTracker.Entry<?>> trackedValues;

    @Inject(method = "<init>(ILnet/minecraft/entity/data/DataTracker;Z)V", at = @At("TAIL"))
    private void golfIV$onConstruction(int id, DataTracker tracker, boolean forceUpdateAll, CallbackInfo ci) {
        // This list is potentially null, so, to mitigate any NPEs, check if it is null before continuing.
        List<DataTracker.Entry<?>> trackedValues = this.trackedValues;
        if (trackedValues == null) return;

        Entity entity = ((DataTrackerAccessor) tracker).getTrackedEntity();

        if (golfConfig.packet.removeHealthTags && entity instanceof LivingEntity && entity.isAlive() && !(entity instanceof Saddleable)) {
            trackedValues.removeIf(trackedValue -> trackedValue.getData() == LIVING_ENTITY_HEALTH);
            trackedValues.removeIf(trackedValue -> trackedValue.getData() == PLAYER_ENTITY_ABSORPTION);

            if (entity instanceof IronGolemEntity || entity instanceof WitherEntity) {
                // Reinjects the health data aligned to quarters.
                LivingEntity livingEntity = (LivingEntity) entity;

                // This takes away 1, divides by 25, floors, multiplies and add 1,
                // spoofing health to be within 25 of the actual value.
                // This allows for the iron golem to be visually broken, and for the wither to have its shield.
                Float newHealth = MathHelper.floor((livingEntity.getHealth() - 1F) / 25F) * 25F + 1F;

                DataTracker.Entry<Float> fakeEntry = new DataTracker.Entry<>(LIVING_ENTITY_HEALTH, newHealth);
                trackedValues.add(fakeEntry);
            }
        } else if (golfConfig.packet.removeDroppedItemInfo && entity instanceof ItemEntity) {
            boolean removed = trackedValues.removeIf(entry -> entry.getData() == ITEM_ENTITY_STACK); // Original item
            if (removed) {
                ItemStack original = ((ItemEntity) entity).getStack();

                DataTracker.Entry<ItemStack> fakeEntry = new DataTracker.Entry<>(ITEM_ENTITY_STACK, fakeStack(original, false));
                trackedValues.add(fakeEntry);
            }
        }
    }
}
