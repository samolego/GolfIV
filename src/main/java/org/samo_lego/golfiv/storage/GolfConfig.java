package org.samo_lego.golfiv.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

import static org.samo_lego.golfiv.utils.BallLogger.logError;

public class GolfConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Main part of the config.
     */
    public static class Main {
        /**
         * Checks item picked that come in players inventory,
         * whether they have disallowed enchantments. This
         * can be disabled if you believe that players
         * didn't spawn in illegals.
         *
         * Status: working
         */
        public boolean checkForStrangeItems = true;

        /**
         * Clears NBT items, but still allows block-picking.
         *
         * Status: working
         */
        public boolean preventCreativeStrangeItems = true;

        /**
         * Checks whether is doing actions
         * that cannot be done while having the GUI open.
         * (e. g. hitting, moving, etc.)
         *
         * Status: working, waiting for falses to be reported.
         */
        public boolean checkIllegalActions = true;

        /**
         * Disables kicks and logs them to console.
         */
        public boolean developerMode = false;

        /**
         * After how many kicks player should be banned.
         *
         * Set to -1 to disable.
         */
        public int maxKicks = 10;

        /**
         * How many cheats should player have in the log to in order to be banned.
         */
        public int minBanCheats = 3;

        /**
         * How many seconds should pass between clearing each element
         * from the cheat list
         */
        public int cheatListClearSeconds = 100;
    }

    /**
     * Outgoing packet settings.
     */
    public static class Packet {
        /**
         * Whether to remove the teleport data
         * from packets when entities move out of
         * view distance.
         *
         * Status: working
         */
        public boolean removeTeleportData = true;

        /**
         * Removes entity health data from packets
         * sent to client.
         *
         * Status: working.
         */
        public boolean removeHealthTags = true;

        /**
         * Removes entity equipment tags from
         * packets. Players will still see if item is enchanted,
         * but won't get the durability or stack size information.
         *
         * Status: working.
         */
        public boolean removeEquipmentTags = true;

        /**
         * Whether to check if player is using
         * SkinBlinker hack.
         */
        public boolean checkSkinBlink = true;

        /**
         * Whether to check if player is using
         * Portals hack.
         */
        public boolean checkPortalHack = true;

        /**
         * Whether to remove original coordinates for the
         * sound, e.g. when summoning a wither / when lightning bolt strikes.
         */
        public boolean patchSoundExploits = true;
    }

    /**
     * Movement checks settings.
     */
    public static class Movement {
        private final String _comment = "Movement checks settings";
        /**
         * Client can tell server its onGround status and
         * server blindly accepts it. This can allow
         * client to not take any fall damage.
         * This setting re-enables the check server-side
         * and doesn't care about the client's onGround status.
         *
         * Status: working (might throw false positives when lagging, not sure)
         */
        public boolean yesFall = true;
        /**
         * Detects flight, either it being
         * a boat-flight or normal player flight
         *
         * Status: working, waiting for falses to be reported.
         */
        public boolean checkFlight = true;
        /**
         * Tries to detect the timer check, which allows
         * client to speed up the game, in order to move faster or use items
         * faster.
         *
         * Status: working, waiting for falses to be reported.
         */
        public boolean antiTimer = true;
        /**
         * Tries to detect speed hacks.
         * Kinda works, but needs overhaul.
         *
         * Status: working, waiting for falses to be reported.
         */
        public boolean noSpeed = true;

        /**
         * Tries detect "fake" elytra movement.
         *
         * Status: working, waiting for falses to be reported.
         */
        public boolean checkElytraFlight = true;

        /**
         * Checks for SafeWalk hacks.
         */
        public boolean hazardousWalk = true;
    }

    /**
     * Combat checks settings.
     */
    public static class Combat {
        private final String _comment = "Combat checks settings.";
        /**
         * Checks for killaura by summoning a fake player.
         *
         * Status: working, waiting for falses to be reported.
         */
        public boolean checkKillaura = true;

        /**
         * Checks for antiknocback hack.
         *
         * Status: working, waiting for FPs to be reported.
         */
        public boolean checkAntiKnockback = true;

        /**
         * Checks if player is hitting entity through wall.
         *
         * Status: working, waiting for falses to be reported.
         */
        public boolean checkWallHit = true;

        /**
         * Checks if player is using reach hacks.
         *
         * Status: working, waiting for falses to be reported.
         */
        public boolean checkHitDistance = true;

        /**
         * Checks the angle at which player is hitting the entity.
         *
         * Status: working, waiting for falses to be reported.
         */
        public boolean checkHitAngle = true;

        /**
         * What hit accuracy counts as suspicious, in percentage.
         *
         * If player is hitting entities with higher accuracy, a fake player will spawn behind
         * them to check for killaura hacks.
         */
        public byte minSuspiciousAccuracy = 80;
    }

    /**
     * Hack "weights", used when reporting.
     */
    public static class SusLevels {

        /**
         * Suspicion value decreases by 1 each X
         * seconds.
         */
        public int suspicionLevelDecreaseTime = 30;

        /**
         * Reports below this value won't count
         * (this is to ensure FP reports aren't showing up in cheat log)
         */
        public int toleratedSuspicionValue = 100;

        /**
         * Players won't be kicked until their sus value reaches this.
         */
        public int reportSuspicionValue = 200;

        private final String _comment = "How much should the suspicion value increase per each cheat attempt.";
        public byte flyHack = 20;
        public byte speedHack = 15;
        public byte timer = 20;
        public byte elytraFlight = 40;
        public byte noFall = 30;
        public byte Jesus = 40;
        public byte skinBlinker = 100;
        public byte inventoryWalk = 20;
        public byte portalHack = 100;
        public byte killaura = 80;
        public byte noHandSwing = 60;
        public byte antiknockback = 20;
        public byte reach = 20;
        public byte hitThroughWalls = 10;
        public byte nbtItems = 100;
        public byte opCreativeItems = 100;
        public byte safeWalk = 15;
    }

    /**
     * Where to log cheaters.
     */
    public static class Logging {
        /**
         * Logs cheat attempts to console.
         *
         * Status: working
         */
        public boolean toConsole = true;
        /**
         * Logs cheat attempts to ops.
         *
         * Status: not yet implemented
         */
        public boolean toOps = true;

        /**
         * How many times a cheat should be recorded before logging it to players/console.
         *
         * This is to ensure you don't get spammed.
         */
        public int logEveryXAttempts = 20;
    }

    /**
     * Which messages should be used when kicking client on cheat attempts.
     * Messages are chosen randomly.
     *
     * Status: working
     */
    public ArrayList<String> kickMessages = new ArrayList<>(Arrays.asList(
            "Only who dares wins!",
            "Bad Liar ...",
            "Script kiddo?",
            "No risk it, no biscuit!",
            "Playing God? How about no?",
            "Who flies high falls low",
            "If you cheat, you only cheat yourself.",
            "I'm not upset that you lied to me,\n I'm upset that from now on I can't believe you.",
            "Hax bad.",
            "You better check your client. It seems to be lying.",
            "Impossible = cannot be done. But it was done by you?",
            "If you have great power, you should\n use it with even greater responsibility."
    ));

    public final GolfConfig.Main main = new Main();
    public final GolfConfig.Combat combat = new Combat();
    public final GolfConfig.Packet packet = new Packet();
    public final GolfConfig.Movement movement = new Movement();
    public final GolfConfig.Logging logging = new Logging();
    public final GolfConfig.SusLevels sus = new SusLevels();

    /**
     * Loads GolfIV config from file.
     *
     * @param configFile file to read GolfIV config from.
     * @return GolfConfig object
     */
    public static GolfConfig loadConfig(File configFile) {
        GolfConfig golfConfig;
        if(configFile.exists() && configFile.isFile()) {
            try(
                    FileInputStream fileInputStream = new FileInputStream(configFile);
                    InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            ) {
                golfConfig = GSON.fromJson(bufferedReader, GolfConfig.class);
            }
            catch (IOException e) {
                throw new RuntimeException("[GolfIV] Problem occurred when trying to load config: ", e);
            }
        }
        else {
            golfConfig = new GolfConfig();
        }
        golfConfig.saveConfig(configFile);

        return golfConfig;
    }

    /**
     * Saves GolfIV config to the file.
     *
     * @param configFile file where to save config to.
     */
    public void saveConfig(File configFile) {
        try (
                FileOutputStream stream = new FileOutputStream(configFile);
                Writer writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8)
        ) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            logError("Problem occurred when saving config: " + e.getMessage());
        }
    }
}
