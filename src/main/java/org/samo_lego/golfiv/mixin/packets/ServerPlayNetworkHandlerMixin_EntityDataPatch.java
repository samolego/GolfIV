package org.samo_lego.golfiv.mixin.packets;// Created 2021-30-06T12:17:18

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Saddleable;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.samo_lego.golfiv.mixin.accessors.ItemEntityAccessor;
import org.samo_lego.golfiv.mixin.accessors.LivingEntityAccessor;
import org.samo_lego.golfiv.mixin.accessors.PlayerEntityAccessor;
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
@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin_EntityDataPatch {
    private static final TrackedData<Float> LIVING_ENTITY_HEALTH = LivingEntityAccessor.getHealth();
    private static final TrackedData<Float> PLAYER_ENTITY_ABSORPTION = PlayerEntityAccessor.getAbsorption();
    private static final TrackedData<ItemStack> ITEM_ENTITY_STACK = ItemEntityAccessor.getSTACK();

    @Shadow
    public abstract ServerPlayerEntity getPlayer();

    @Inject(method = "sendPacket(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;)V", at = @At("TAIL"))
    private void golfIV$onConstruction(Packet<?> unknownPacket, @Nullable PacketCallbacks callbacks, CallbackInfo ci) {

        if (unknownPacket instanceof EntityTrackerUpdateS2CPacket packet) {
            List<DataTracker.SerializedEntry<?>> trackedValues = packet.trackedValues();
            // This list is potentially null, so, to mitigate any NPEs, check if it is null before continuing.
            if (trackedValues == null) return;


            Entity entity = this.getPlayer().getEntityWorld().getEntityById(packet.id());

            if (golfConfig.packet.removeHealthTags && entity instanceof LivingEntity livingEntity && entity.isAlive() && !(entity instanceof Saddleable)) {
                trackedValues.removeIf(trackedValue -> trackedValue.value() == LIVING_ENTITY_HEALTH);
                trackedValues.removeIf(trackedValue -> trackedValue.value() == PLAYER_ENTITY_ABSORPTION);

                // This allows for iron golems to be visually broken, withers to have their shields,
                // and wolves to show their health, while still spoofing the health to a variable degree.
                // This is editable in GolfConfig as allowHealthTags.
                if (golfConfig.packet.allowedHealthTags.containsKey(entity.getType())) {
                    float percentage = golfConfig.packet.allowedHealthTags.getFloat(entity.getType());
                    float divider = livingEntity.getMaxHealth() * percentage;

                    // Shortcuts to livingEntity.getHealth on <= 1F.
                    Float newHealth = divider <= 1F ? livingEntity.getHealth() :
                            MathHelper.floor((livingEntity.getHealth() - 1F) / divider) * divider + 1F;

                    var fakeEntry = DataTracker.SerializedEntry.of(LIVING_ENTITY_HEALTH, newHealth);
                    trackedValues.add(fakeEntry);
                }
            } else if (golfConfig.packet.removeDroppedItemInfo && entity instanceof ItemEntity itemEntity) {
                boolean removed = trackedValues.removeIf(entry -> entry.value() == ITEM_ENTITY_STACK); // Original item
                if (removed) {
                    ItemStack original = itemEntity.getStack();

                    DataTracker.SerializedEntry<ItemStack> fakeEntry = DataTracker.SerializedEntry.of(ITEM_ENTITY_STACK, fakeStack(original, false));
                    trackedValues.add(fakeEntry);
                }
            }
        }
    }
}
