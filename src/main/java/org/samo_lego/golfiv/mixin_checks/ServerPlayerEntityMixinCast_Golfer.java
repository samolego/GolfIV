package org.samo_lego.golfiv.mixin_checks;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import org.samo_lego.golfiv.casts.Golfer;
import org.samo_lego.golfiv.mixin_checks.accessors.EntityAccessor;
import org.samo_lego.golfiv.utils.BallLogger;
import org.samo_lego.golfiv.utils.CheatType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static org.samo_lego.golfiv.GolfIV.golfConfig;

/**
 * Additional methods and fields for PlayerEntities.
 */
@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixinCast_Golfer implements Golfer {

    @Shadow @Final public MinecraftServer server;
    private final ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;

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
    private int guiOpenInPortalTicks;

    @Unique
    private short kicks;

    @Unique
    private ListTag cheatLog;

    @Unique
    private int hackAttempts;

    @Unique
    private int entityHits = 0;
    @Unique
    private int handSwings = 1;

    @Unique
    private boolean nearFluid;

    @Unique
    private final LinkedList<CheatType> CHEATS = new LinkedList<>();


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

    @Override
    public void setHitAccuracy(int entityHits, int handSwings) {
        this.entityHits = entityHits;
        this.handSwings = handSwings;
    }
    @Override
    public void setHitAccuracy(int accuracy) {
        this.entityHits = accuracy;
        this.handSwings = 100;
    }

    @Override
    public int getHitAccuracy() {
        return this.entityHits * 100 / this.handSwings;
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

    @Override
    public short getKicks() {
        return this.kicks;
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
     * Tells whether player is near fluid (above).
     *
     * @return true if player is near fluid collisions, otherwise false.
     */
    @Override
    public boolean isNearFluid() {
        return this.nearFluid;
    }

    /**
     * Sets the nearFluid status.
     *
     * @param nearFluid if player is near (above) fluid
     */
    @Override
    public void setNearFluid(boolean nearFluid) {
        this.nearFluid = nearFluid;
    }

    /**
     * Reports player for cheating / kicks them.
     * (will be changed in future)
     *
     * @param cheatType type of the cheat player has used.
     */
    @Override
    public void report(CheatType cheatType, int susValue) {
        this.susLevel += this.CHEATS.size() * susValue;

        if(this.CHEATS.size() > 0 && cheatType.equals(this.CHEATS.getLast())) {
            ++this.hackAttempts;
        }
        else {
            this.hackAttempts = 1;
            this.CHEATS.add(cheatType);
        }

        if(this.susLevel < golfConfig.sus.toleratedSuspicionValue)
            return;


        // Saving cheat log
        LocalDateTime now = LocalDateTime.now();
        if(cheatLog == null) {
            cheatLog = new ListTag();
        }
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
                cheat.putInt("times_used", 1);

                cheatLog.add(cheat);
            }
        }
        else {
            CompoundTag cheat = new CompoundTag();
            cheat.putString("type", cheatType.getCheat());
            cheat.putString("time", now.toString());
            cheat.putInt("times_used", 1);

            cheatLog.add(cheat);
        }



        if(this.hackAttempts % golfConfig.logging.logEveryXAttempts == 0) {
            String msg = "§6[GolfIV] §2Suspicion value of §b" + this.player.getGameProfile().getName() + "§2 has reached §d" + this.susLevel + "§2.";
            Text text = new LiteralText(msg).styled((style) -> style.withColor(Formatting.GREEN).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Last cheat: " + cheatType.getCheat() + ", used: " + this.hackAttempts + "x."))));

            if(golfConfig.logging.toOps) {
                List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
                for(ServerPlayerEntity p : players) {
                    if(p.hasPermissionLevel(4)) {
                        p.sendMessage(text, false);
                    }
                }
            }
            if(golfConfig.logging.toConsole) {
                BallLogger.logInfo(this.player.getGameProfile().getName() + " is probably using " + cheatType.getCheat() + " hack(s).");
            }
        }

        if(this.susLevel > golfConfig.sus.reportSuspicionValue) {
            this.susLevel = 0;
            if(this.CHEATS.size() > golfConfig.main.minBanCheats && golfConfig.main.maxKicks != -1 && ++this.kicks > golfConfig.main.maxKicks) {
                this.kicks = 0;
                if(!golfConfig.main.developerMode)
                    player.networkHandler.disconnect(new LiteralText(
                            "§c[Ban from GolfIV (not really)]\n§6" +
                                    golfConfig.kickMessages.get(new Random().nextInt(golfConfig.kickMessages.size()
                                    ))
                    ));
                else
                    BallLogger.logInfo(this.player.getGameProfile().getName() + " should be BANNED.");
            }
            else {
                if(!golfConfig.main.developerMode)
                    player.networkHandler.disconnect(new LiteralText(
                            "§3[GolfIV]\n§a" +
                                    golfConfig.kickMessages.get(new Random().nextInt(golfConfig.kickMessages.size()
                                    ))
                    ));
                else
                    BallLogger.logInfo(this.player.getGameProfile().getName() + " should be KICKED.");
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
     * Gets the number of ticks that the player
     * has had an open GUI while in a nether portal
     *
     * @return number of ticks, greater than or equal to 0
     */
    @Override
    public int getGuiOpenInPortalTicks() {
        return this.guiOpenInPortalTicks;
    }

    /**
     * Sets the number of ticks that the player
     * has had an open GUI while in a nether portal
     *
     * @param ticks
     */
    @Override
    public void setGuiOpenInPortalTicks(int ticks) {
        this.guiOpenInPortalTicks = ticks;
    }

    /**
     * Lowers the susLevel by 1 each half a minute.
     *
     * @param ci
     */
    @Inject(method = "tick()V", at = @At("TAIL"))
    public void tick(CallbackInfo ci) {
        if(++this.ticks == golfConfig.sus.suspicionLevelDecreaseTime * 20) {
            this.ticks = 0;
            this.susLevel -= this.susLevel > 0 ? 1 : 0;
        }
        if(this.ticks % (golfConfig.main.cheatListClearSeconds * 20) == 0 && this.CHEATS.size() > 1)
            this.CHEATS.pop();
    }

    @Inject(method = "copyFrom(Lnet/minecraft/server/network/ServerPlayerEntity;Z)V", at = @At("TAIL"))
    private void copyFromPlayer(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        this.cheatLog = ((Golfer) oldPlayer).getCheatLog();
        this.susLevel = ((Golfer) oldPlayer).getSusLevel();
        this.kicks = ((Golfer) oldPlayer).getKicks();
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

        if(this.cheatLog != null)
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
