package org.samo_lego.golfiv.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.samo_lego.golfiv.storage.GolfConfig;

import java.io.File;

import static net.minecraft.server.command.CommandManager.literal;
import static org.samo_lego.golfiv.GolfIV.golfConfig;

public class GolfCommand {

    public static void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("golf")
                .requires(source -> source.hasPermissionLevel(4))
                .then(CommandManager.literal("reloadConfig")
                        .executes( ctx -> reloadConfig(ctx.getSource()))
                )
        );
    }

    public static int reloadConfig(ServerCommandSource source) {
        golfConfig = GolfConfig.loadConfig(new File(FabricLoader.getInstance().getConfigDir() + "/GolfIV_config.json"));

        if(source != null)
            source.sendFeedback(
                    Text.literal("Reloaded the config file!")
                        .formatted(Formatting.GREEN),
                    false
            );
        return 1;
    }
}
