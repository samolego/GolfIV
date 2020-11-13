package org.samo_lego.golfiv.utils;


import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.PlayerManager;
import net.minecraft.text.LiteralText;

import java.util.Objects;
import java.util.Random;

public class FakePlayer {

    public static void alert(PlayerEntity player) {
        PlayerManager playerManager = Objects.requireNonNull(player.getServer()).getPlayerManager();
        PlayerEntity pl = playerManager.getPlayerList().get(new Random().nextInt());
        player.sendMessage(new LiteralText(
                pl.getGameProfile().getName() + ", why u hacking?"
        ), false);
    }
}
