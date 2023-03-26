package org.samo_lego.golfiv.event.S2CPacket;

import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import org.samo_lego.golfiv.mixin.accessors.PlaySoundS2CPacketAccessor;

import java.util.Optional;

import static net.minecraft.sound.SoundEvents.*;
import static org.samo_lego.golfiv.GolfIV.golfConfig;

public class SoundCoordinatesPatch implements S2CPacketCallback {
    public SoundCoordinatesPatch() {
    }
    /**
     * Removes certain sound coordinates.
     *
     * @param packet packet being sent
     * @param player player getting the packet
     * @param server Minecraft Server
     */

    @Override
    public void preSendPacket(Packet<?> packet, ServerPlayerEntity player, MinecraftServer server) {
        if(golfConfig.packet.patchSoundExploits && packet instanceof PlaySoundS2CPacket) {
            PlaySoundS2CPacketAccessor packetAccessor = (PlaySoundS2CPacketAccessor) packet;
            Optional<RegistryKey<SoundEvent>> soundEvt = packetAccessor.getSound().getKey();

            if (soundEvt.isEmpty()) {
                return;
            }

            SoundEvent sound = Registries.SOUND_EVENT.get(soundEvt.get());


            if (
                    ENTITY_LIGHTNING_BOLT_THUNDER.equals(sound) ||
                            ENTITY_LIGHTNING_BOLT_IMPACT.equals(sound) ||
                            ENTITY_WITHER_SPAWN.equals(sound) /*||
                    BLOCK_END_PORTAL_FRAME_FILL.equals(sound)*/ //not sure about this sound
            ) {
                // Global sounds which can be used to track players
                int maxPlayerDistance = server.getPlayerManager().getViewDistance() * 16;
                int deltaX = (int) (player.getX() - packetAccessor.getX());
                int deltaZ = (int) (player.getZ() - packetAccessor.getZ());
                int actualPlayerDistance = deltaX * deltaZ;

                if (maxPlayerDistance * maxPlayerDistance < Math.abs(actualPlayerDistance)) {
                    // We shouldn't send the right coords
                    packetAccessor.setX(maxPlayerDistance);
                    packetAccessor.setZ(maxPlayerDistance);
                }
            }
        }
    }
}
