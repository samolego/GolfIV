package org.samo_lego.golfiv.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.samo_lego.golfiv.casts.Golfer;
import org.samo_lego.golfiv.storage.GolfConfig;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import static net.minecraft.server.command.CommandManager.literal;
import static org.samo_lego.golfiv.GolfIV.golfConfig;

public class GolfCommand {

    public static void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("golf")
                .requires(source -> source.hasPermissionLevel(4))
                .then(CommandManager.literal("reloadConfig")
                        .executes( ctx -> reloadConfig(ctx.getSource().getEntity()))
                )
                .then(CommandManager.literal("player")
                        .then(CommandManager.argument("targets", EntityArgumentType.players())
                                .then(CommandManager.literal("setSuspicionLevel")
                                        .then(CommandManager.argument("level", IntegerArgumentType.integer(0))
                                                .executes(ctx -> setSusLevel(ctx.getSource(), EntityArgumentType.getPlayers(ctx, "targets"), IntegerArgumentType.getInteger(ctx, "level")))
                                        )
                                )
                                .then(CommandManager.literal("getSuspicionLevel")
                                        .executes(ctx -> getSusLevel(ctx.getSource(), EntityArgumentType.getPlayers(ctx, "targets")))
                                )
                                .executes(ctx -> getSusLevel(ctx.getSource(), EntityArgumentType.getPlayers(ctx, "targets")))
                        )
                )
        );
    }

    private static int setSusLevel(ServerCommandSource source, Collection<ServerPlayerEntity> targets, int level) {
        for(ServerPlayerEntity player : targets) {
            ((Golfer) player).setSusLevel(level);
        }

        source.sendFeedback(new LiteralText("The suspicion value was successfully set to " + level).formatted(Formatting.GREEN), false);

        return 1;
    }

    private static int getSusLevel(ServerCommandSource source, Collection<ServerPlayerEntity> targets) {
        StringBuilder message = new StringBuilder();
        Iterator<ServerPlayerEntity> iterator = targets.iterator();

        message.append("§b>--------< GolfIV suspicion values >--------<§r\n");
        while(iterator.hasNext()) {
            ServerPlayerEntity player = iterator.next();
            int susLevel  = ((Golfer) player).getSusLevel();

            message.append("§2Player §5").append(player.getGameProfile().getName()).append("§2 has a suspicion level of §6").append(susLevel).append("§2.");

            //if(iterator.hasNext())
                message.append("\n");
        }
        message.append("§b>---------------------------------------<§r");
        source.sendFeedback(new LiteralText(message.toString()), false);

        return 1;
    }

    public static int reloadConfig(Entity sender) {
        golfConfig = GolfConfig.loadConfig(new File(FabricLoader.getInstance().getConfigDir() + "/GolfIV_config.json"));

        if(sender != null)
            ((PlayerEntity) sender).sendMessage(new LiteralText("Reloaded the config file!").formatted(
                    Formatting.GREEN
            ), false);
        return 1;
    }
}
