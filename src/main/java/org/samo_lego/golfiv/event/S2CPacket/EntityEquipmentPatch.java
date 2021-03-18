package org.samo_lego.golfiv.event.S2CPacket;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.samo_lego.golfiv.mixin.accessors.EntityEquipmentUpdateS2CPacketAccessor;

import java.util.List;

import static org.samo_lego.golfiv.GolfIV.golfConfig;

public class EntityEquipmentPatch implements S2CPacketCallback {

    public EntityEquipmentPatch() {
    }

    /**
     * Loops through entity equipment packet data
     * and removes attributes that cannot be seen.
     *
     * @param packet packet being sent
     * @param player player getting the packet
     * @param server Minecraft Server
     */
    @Override
    public void preSendPacket(Packet<?> packet, ServerPlayerEntity player, MinecraftServer server) {
        if(golfConfig.packet.removeEquipmentTags && packet instanceof EntityEquipmentUpdateS2CPacket) {
            EntityEquipmentUpdateS2CPacketAccessor packetAccessor = (EntityEquipmentUpdateS2CPacketAccessor) packet;

            if(packetAccessor.getEntityId() == player.getEntityId())
                return;

            List<Pair<EquipmentSlot, ItemStack>> newEquipment = Lists.newArrayList();
            packetAccessor.getEquipment().forEach(pair -> {
                ItemStack stack = pair.getSecond();

                ItemStack fakedStack = new ItemStack(stack.getItem(), stack.getMaxCount());
                if(stack.hasEnchantments())
                    fakedStack.addEnchantment(null, 0);

                newEquipment.add(new Pair<>(pair.getFirst(), fakedStack));
            });

            packetAccessor.setEquipment(newEquipment);
        }
    }
}
