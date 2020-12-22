package org.samo_lego.golfiv.mixin_checks;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import org.samo_lego.golfiv.casts.Golfer;
import org.samo_lego.golfiv.utils.BallLogger;
import org.samo_lego.golfiv.utils.CheatType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import static org.samo_lego.golfiv.GolfIV.golfConfig;

/**
 * Additional methods and fields for PlayerEntities.
 */
@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixinCast_Golfer implements Golfer {

    private final PlayerEntity player = (PlayerEntity) (Object) this;

    @Unique
    private boolean blockCollisions, entityCollisions, hasOpenScreen;

    /**
     * The sus level of the player
     *
     * 0 - 100 = nothing at all (FPs etc.)
     * 100 - 1000 = kickable (likely to be using hacks)
     * > 1000 = bannable (surely using hacks)
     */
    @Unique
    private int susLevel;

    @Unique
    private int ticks;
    @Unique
    private short kicks;

    @Unique
    private ListTag cheatLog;


    /**
     * Clears cheat log from the player.
     */
    @Override
    public void clearCheatLog() {
        this.cheatLog = new ListTag();
    }

    /**
     * Gets cheat log for the player.
     */
    @Override
    public ListTag getCheatLog() {
        return this.cheatLog;
    }

    /**
     * Gets the suspicion value for the player.
     *
     * @return suspicion value, higher than 0
     */
    @Override
    public int getSusLevel() {
        return this.susLevel;
    }

    /**
     * Sets suspicion value for the player.
     */
    @Override
    public void setSusLevel(int newSusLevel) {
        this.susLevel = newSusLevel;
    }

    /**
     * Real onGround value, which isn't affected
     * by the client packet.
     *
     * @return true if player is near ground (0.5001 block tolerance), otherwise false.
     */
    @Override
    public boolean isNearGround() {
        return blockCollisions || entityCollisions;
    }

    /**
     * Sets whether player has block collisions.
     *
     * @param blockCollisions whether player has block collisions.
     */
    @Override
    public void setBlockCollisions(boolean blockCollisions) {
        this.blockCollisions = blockCollisions;
    }

    /**
     * Sets whether player has entity collisions (e. g. boat collisions).
     *
     * @param entityCollisions whether player has entity collisions.
     */
    @Override
    public void setEntityCollisions(boolean entityCollisions) {
        this.entityCollisions = entityCollisions;
    }

    /**
     * Tells whether player has entity collisions.
     *
     * @return true if player has entity collisions, otherwise false.
     */
    @Override
    public boolean hasEntityCollisions() {
        return entityCollisions;
    }

    /**
     * Reports player for cheating / kicks them.
     * (will be changed in future)
     *
     * @param cheatType type of the cheat player has used.
     */
    @Override
    public void report(CheatType cheatType, int susValue) {
        this.susLevel += susValue;
        if(this.susLevel < 100)
            return;


        // Saving cheat log
        LocalDateTime now = LocalDateTime.now();
        if(cheatLog.size() > 0) {
            CompoundTag lastCheat = cheatLog.getCompound(cheatLog.size() - 1);
            if(lastCheat != null && cheatType.getCheat().equals(lastCheat.getString("type"))) {
                int usages = lastCheat.getInt("times_used");
                lastCheat.putInt("times_used", ++usages);
            }
            else {
                CompoundTag cheat = new CompoundTag();
                cheat.putString("type", cheatType.getCheat());
                cheat.putString("time", now.toString());

                cheatLog.add(cheat);
            }
        }
        else {
            CompoundTag cheat = new CompoundTag();
            cheat.putString("type", cheatType.getCheat());
            cheat.putString("time", now.toString());

            cheatLog.add(cheat);
        }


        if(player instanceof ServerPlayerEntity) {
            final ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) player;

            String msg = "§6[GolfIV] §2Suspicion value of §b" + player.getGameProfile().getName() + "§2 has reached §d" + this.susLevel + "§2.";
            Text text = new LiteralText(msg).styled((style) -> style.withColor(Formatting.GREEN).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Last cheat: " + cheatType.getCheat()))));

            if(golfConfig.logging.toOps) {
                List<ServerPlayerEntity> players = serverPlayerEntity.getServer().getPlayerManager().getPlayerList();
                for(ServerPlayerEntity p : players) {
                    if(p.hasPermissionLevel(4)) {
                        p.sendMessage(text, false);
                    }
                }
            }
            if(golfConfig.logging.toConsole) {
                BallLogger.logInfo(player.getGameProfile().getName() + " is probably using " + cheatType.getCheat() + " hack(s).");
            }

            if(this.susLevel > 200) {
                this.susLevel = 0;
                if(++this.kicks > 10) {
                    this.kicks = 0;
                    if(!golfConfig.main.developerMode)
                        serverPlayerEntity.networkHandler.disconnect(new LiteralText(
                                "§c[Ban from GolfIV (not really)]\n§6" +
                                        golfConfig.kickMessages.get(new Random().nextInt(golfConfig.kickMessages.size()
                                        ))
                        ));
                    else
                        BallLogger.logInfo(player.getGameProfile().getName() + " should be BANNED.");
                }
                else {
                    if(!golfConfig.main.developerMode)
                        serverPlayerEntity.networkHandler.disconnect(new LiteralText(
                                "§3[GolfIV]\n§a" +
                                        golfConfig.kickMessages.get(new Random().nextInt(golfConfig.kickMessages.size()
                                        ))
                        ));
                    else
                        BallLogger.logInfo(player.getGameProfile().getName() + " should be KICKED.");
                }
            }
        }
    }

    /**
     * Sets whether player has opened GUI.
     * Doesn't catch opening their own inventory.
     *
     * @param openGui whether player has opened the GUI.
     */
    @Override
    public void setOpenGui(boolean openGui) {
        this.hasOpenScreen = openGui;
    }

    /**
     * Tells whether player has open GUI.
     * Doesn't catch their own inventory being open.
     *
     * @return true if player has open GUI, otherwise false
     */
    @Override
    public boolean hasOpenGui() {
        return this.hasOpenScreen;
    }

    /**
     * Lowers the susLevel by 1 each half a minute.
     *
     * @param ci
     */
    @Inject(method = "tick()V", at = @At("TAIL"))
    public void tick(CallbackInfo ci) {
        if(++this.ticks == 600) {
            this.ticks = 0;
            this.susLevel -= this.susLevel > 0 ? 1 : 0;
        }
    }

    /**
     * Saves susLevel to player data.
     *
     * @param tag
     * @param ci
     */
    @Inject(method = "writeCustomDataToTag(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("TAIL"))
    private void writeCustomDataToTag(CompoundTag tag, CallbackInfo ci) {
        CompoundTag dataTag = new CompoundTag();

        dataTag.putInt("sus_lvl", this.susLevel);
        dataTag.putShort("kicks", this.kicks);
        dataTag.put("cheat_log", this.cheatLog);

        tag.put("golfIV:player_data", dataTag);
    }

    /**
     * Reads susLevel from player data.
     *
     * @param tag
     * @param ci
     */
    @Inject(method = "readCustomDataFromTag(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("TAIL"))
    private void readCustomDataFromTag(CompoundTag tag, CallbackInfo ci) {
        CompoundTag dataTag = tag.getCompound("golfIV:player_data");
        if(dataTag != null) {
            this.susLevel = dataTag.contains("sus_lvl") ? dataTag.getInt("sus_lvl") : 0;
            this.kicks = dataTag.contains("kicks") ? dataTag.getShort("kicks") : 0;
            this.cheatLog = dataTag.contains("cheat_log") ? (ListTag) dataTag.get("cheat_log") : new ListTag();
        }
    }
}
