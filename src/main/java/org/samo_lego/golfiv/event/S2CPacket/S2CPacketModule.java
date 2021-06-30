package org.samo_lego.golfiv.event.S2CPacket;


public class S2CPacketModule {

    public static void registerEvents() {
        S2CPacketCallback.EVENT.register(new EntityEquipmentPatch());
        S2CPacketCallback.EVENT.register(new SoundCoordinatesPatch());
        S2CPacketCallback.EVENT.register(new EntityTeleportDataPatch());
        S2CPacketCallback.EVENT.register(new ItemInventoryKickPatch());
    }
}
