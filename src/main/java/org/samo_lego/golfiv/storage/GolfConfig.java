package org.samo_lego.golfiv.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;

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
         */
        public boolean yesFall = true;
    }

    /**
     * Combat checks settings.
     */
    public static class Combat {
        private final String _comment = "Combat checks settings.";
        /**
         * Checks if player is hitting entity through wall.
         */
        public boolean checkWallHit = true;

        /**
         * Checks if player is using reach hacks.
         */
        public boolean checkHitDistance = true;

        /**
         * Checks the angle at which player is hitting the entity.
         */
        public boolean checkHitAngle = true;
    }

    public final GolfConfig.Main main = new Main();
    public final GolfConfig.Combat combat = new Combat();
    public final GolfConfig.Packet packet = new Packet();
    public final GolfConfig.Movement movement = new Movement();

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
