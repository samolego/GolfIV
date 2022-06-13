package org.samo_lego.golfiv.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import it.unimi.dsi.fastutil.objects.Object2FloatMaps;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.samo_lego.golfiv.utils.BallLogger.logError;

public class GolfConfig {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    /**
     * Main part of the config.
     */
    public static class Main {

        public final String _comment_checkInventoryActions = "// Prevents hitting/chatting with open GUI";
        /**
         * Checks whether is doing actions
         * that cannot be done while having the GUI open.
         * (e. g. hitting, typing, etc.)
         */
        public boolean checkInventoryActions = true;

        public final String _comment_preventDestructionByPiston = "// Prevents headless pistons from destroying blocks that are not piston extensions.";

        public boolean preventDestructionByHeadlessPistons = true;

        public final String _comment_allowedDestructibleByPiston_1 = "// Allows headless pistons to destroy certain blocks when preventing destruction is enabled.";
        public final String _comment_allowedDestructibleByPiston_2 = "// Useful to allow only breaking of bedrock but denying destruction of barriers.";

        /**
         * Allows headless pistons to destroy certain blocks when {@link #preventDestructionByHeadlessPistons} is enabled.
         * <p>
         * Useful to allow only breaking of bedrock but denying destruction of barriers, chests and other blocks.
         */
        @JsonAdapter(BlockSetAdapter.class)
        public Set<Block> allowedDestructibleByHeadlessPistons = Collections.singleton(Blocks.PISTON_HEAD);

        @SerializedName("// What altitude in the nether should start inflicting void damage (e.g. 128). -1 disables it.")
        public final String _comment_inflictNetherRoofDamage = "";
        public int inflictNetherRoofDamage = -1;
    }

    /**
     * Checks item picked that come in players inventory,
     * whether they have disallowed enchantments. This
     * can be disabled if you believe that players
     * didn't spawn in illegals.
     */
    public static class IllegalItems {
        public static class Survival {
            public final String _comment_legalise = "// Whether to enable 'legalising' survival items.";

            public final String _comment_legaliseWholeInventory = "// Whether to check whole inventory when connecting / changing dimensions, etc.";
            public boolean legaliseWholeInventory = true;

            public final String _comment_bannedSurvivalItems = "// Which items should be cleared when clicked in survival inventory";
            public ArrayList<String> bannedItems = new ArrayList<>(Arrays.asList(
                    "minecraft:barrier",
                    "minecraft:spawner",
                    "minecraft:structure_void",
                    "minecraft:bedrock",
                    "minecraft:command_block",
                    "minecraft:spawn_egg"
            ));
            public boolean checkEnchants = true;
            public boolean checkPotionLevels = true;
            public boolean checkItemCount = true;
        }
        public static class Creative {
            public final String _comment_legaliseWholeInventory = "// Whether to check whole inventory when connecting / changing dimensions, etc.";
            public boolean legaliseWholeInventory = true;

            public final String _comment_whitelistedNBT = "// Which NBT shouldn't be cleared";
            public ArrayList<String> whitelistedNBT = new ArrayList<>(Arrays.asList(
                "EntityTag",
                "Enchantments",
                "StoredEnchantments",
                "BlockEntityTag",
                "Damage",
                "Potion",
                "display"
            ));
            /**
             * Clears NBT items, but still allows block-picking.
             */
            public final String _comment_removeCreativeNBTTags = "// Disallow all NBT tags in creative which aren't in whitelist section.";
            public boolean removeCreativeNBTTags = true;
            public boolean checkEnchants = true;
            public boolean checkPotionLevels = true;
            public boolean checkItemCount = true;
        }

        public Survival survival = new Survival();
        public Creative creative = new Creative();
    }

    /**
     * Outgoing packet settings.
     */
    public static class Packet {
        public final String _comment = "// Patches some outgoing server packets";
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
         * <p>
         * Status: working.
         */
        public boolean removeHealthTags = true;

        public final String _comment_allowedHealthTags_1 = "// Allows health tags for certain entities.";
        public final String _comment_allowedHealthTags_2 = "// This maps entity ID to percentage as decimal.";

        /**
         * Entities that must have health sent to render correctly.
         * <p>
         * K -> Entities to allow health of.
         * V -> Increments by percentage of health to allow.
         * <p>
         * Implied by default is 1F, or alive and dead.
         */
        @JsonAdapter(UnnecessaryEntityTypeMapAdapter.class)
        public Object2FloatOpenHashMap<EntityType<?>> allowedHealthTags = new Object2FloatOpenHashMap<>(
                new EntityType<?>[]{EntityType.WOLF, EntityType.WITHER, EntityType.IRON_GOLEM},
                new float[]{0F, 0.5F, 0.25F}
        );

        /**
         * Removes entity equipment tags from
         * packets. Players will still see if item is enchanted,
         * but won't get the durability or stack size information.
         * <p>
         * Status: working.
         */
        public boolean removeEquipmentTags = true;

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

        /**
         * Whether to remove info about ground items.
         * Can prevent chunk banning with items that are lying on ground.
         */
        public boolean removeDroppedItemInfo = true;

        /**
         * Whether to cancel out sending too big packets.
         * Patches "book-banning" and friends.
         */
        public boolean patchItemKickExploit = true;
    }

    /**
     * Movement checks settings.
     */
    public static class Movement {
        private final String _comment = "// Movement checks settings";
        /**
         * Client can tell server its onGround status and
         * server blindly accepts it. This can allow
         * client to not take any fall damage.
         * This setting re-enables the check server-side
         * and doesn't care about the client's onGround status.
         */
        public boolean yesFall = true;
    }

    public static class Duplication {
        public final String _comment = "// Duplication fixes";
        public final String _comment_patchSaveLimit1 = "// Whether to prevent throwing an error when saving large string data.";
        public final String _comment_patchSaveLimit2 = "// This is done by ignoring data after DataOutputStream limit.";
        public final String _comment_patchSaveLimit3 = "// Written books can reach that point with hacked clients.";
        public boolean patchSaveLimit = true;
        public final String _comment_patchGravityBlock = "// Whether to disable gravity block duping.";
        public boolean patchGravityBlock = true;
        public final String _comment_patchDeathDuplication1 = "// Checks if player is connected before applying damage.";
        public final String _comment_patchDeathDuplication2 = "// Prevents duplicating inventory if player dies after disconnect.";
        public boolean patchDeathDuplication = true;
    }

    /**
     * Combat checks settings.
     */
    public static class Combat {
        private final String _comment = "// Combat checks settings.";
        /**
         * Checks if player is hitting entity through wall.
         */
        public boolean preventWallHit = true;

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
    public final GolfConfig.IllegalItems items = new IllegalItems();
    public final GolfConfig.Combat combat = new Combat();
    public final GolfConfig.Packet packet = new Packet();
    public final GolfConfig.Movement movement = new Movement();
    public final GolfConfig.Duplication duplication = new Duplication();

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
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader)
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

    /**
     * Adapts {@link Block} between it and the identifier.
     *
     * @author KJP12
     */
    private static final class BlockSetAdapter extends TypeAdapter<Set<Block>> {

        @Override
        public void write(JsonWriter out, Set<Block> value) throws IOException {
            out.beginArray();
            var reg = Registry.BLOCK;
            for (var block : value) {
                out.value(reg.getId(block).toString());
            }
            out.endArray();
        }

        @Override
        public Set<Block> read(JsonReader in) throws IOException {
            in.beginArray();
            var reg = Registry.BLOCK;
            var set = new HashSet<Block>();
            while (in.hasNext()) {
                set.add(reg.get(Identifier.tryParse(in.nextString())));
            }
            in.endArray();
            return set;
        }
    }

    /**
     * Adapts {@link EntityType} between it and the identifier.
     * <p>
     * Unnecessary, as map-level shouldn't be needed to begin with,
     * yet arbitrary unforeseen restrictions require this anyways.
     *
     * @author KJP12
     */
    private static final class UnnecessaryEntityTypeMapAdapter extends TypeAdapter<Object2FloatOpenHashMap<EntityType<?>>> {

        @Override
        public void write(JsonWriter out, Object2FloatOpenHashMap<EntityType<?>> value) throws IOException {
            out.beginObject();
            var itr = Object2FloatMaps.fastIterator(value);
            while (itr.hasNext()) {
                var entry = itr.next();
                out.name(EntityType.getId(entry.getKey()).toString());
                out.value(entry.getFloatValue());
            }
            out.endObject();
        }

        @Override
        public Object2FloatOpenHashMap<EntityType<?>> read(JsonReader in) throws IOException {
            in.beginObject();
            var map = new Object2FloatOpenHashMap<EntityType<?>>();
            while (in.hasNext()) {
                map.put(EntityType.get(in.nextName()).orElseThrow(() -> new IOException("Invalid entity type.")),
                        (float) in.nextDouble());
            }
            in.endObject();
            return map;
        }
    }
}
