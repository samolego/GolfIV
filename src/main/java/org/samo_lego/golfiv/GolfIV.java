package org.samo_lego.golfiv;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.samo_lego.golfiv.commands.GolfCommand;
import org.samo_lego.golfiv.event.IllegalBlockBreakCheck;
import org.samo_lego.golfiv.event.S2CPacket.S2CPacketModule;
import org.samo_lego.golfiv.event.combat.CombatModule;
import org.samo_lego.golfiv.storage.GolfConfig;

import java.io.File;

public class GolfIV implements ModInitializer {

	/**
	 * GolfIV config.
	 */
	public static GolfConfig golfConfig;

	/**
	 * Initializes GolfIV.
	 */
	@Override
	public void onInitialize() {
		golfConfig = GolfConfig.loadConfig(new File(FabricLoader.getInstance().getConfigDir() + "/GolfIV_config.json"));

		CommandRegistrationCallback.EVENT.register((dispatcher, ignored, ignored1) ->
				GolfCommand.registerCommand(dispatcher));

		// Events
		CombatModule.registerEvents();
		S2CPacketModule.registerEvents();
		PlayerBlockBreakEvents.BEFORE.register(new IllegalBlockBreakCheck());
	}
}
