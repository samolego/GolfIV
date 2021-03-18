package org.samo_lego.golfiv.mixin.S2CPacket;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.samo_lego.golfiv.mixin.accessors.EntityEquipmentUpdateS2CPacketAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static org.samo_lego.golfiv.GolfIV.golfConfig;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin_EntityEquipment {
    @Shadow public ServerPlayerEntity player;

    /**
     * Loops through entity equipment packet data
     * and removes attributes that cannot be seen.
     *
     * @param packet
     * @param ci
     */
    @Inject(
            method = "sendPacket(Lnet/minecraft/network/Packet;)V",
            at = @At("HEAD")
    )
    private void removeHealthTags(Packet<?> packet, CallbackInfo ci) {
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
