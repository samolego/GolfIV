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
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.samo_lego.golfiv.utils.BallLogger.logError;

public class GolfConfig {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    @SerializedName("// Patches some outgoing server packets")
    public final String _comment_packet = "";

    /**
     * Main part of the config.
     */
    public static class Main {

        @SerializedName("// Prevents hitting/chatting with open GUI")
        public final String _comment_checkInventoryActions = "";
        /**
         * Checks whether is doing actions
         * that cannot be done while having the GUI open.
         * (e. g. hitting, typing, etc.)
         */
        public boolean checkInventoryActions = true;

        @SerializedName("// Prevents headless pistons from destroying blocks that are not piston extensions.")
        public final String _comment_preventDestructionByPiston = "";

        public boolean preventDestructionByHeadlessPistons = true;

        @SerializedName("// Allows headless pistons to destroy certain blocks when preventing destruction is enabled.")
        public final String _comment_allowedDestructibleByPiston_1 = "";

        @SerializedName("// Useful to allow only breaking of bedrock but denying destruction of barriers.")
        public final String _comment_allowedDestructibleByPiston_2 = "";

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
            @SerializedName("// Whether to enable 'legalising' survival items.")
            public final String _comment_legalise = "";

            @SerializedName("// Whether to check whole inventory when connecting / changing dimensions, etc.")

            public final String _comment_legaliseWholeInventory = "";
            public boolean legaliseWholeInventory = true;

            @SerializedName("// Which items should be cleared when clicked in survival inventory")
            public final String _comment_bannedSurvivalItems = "";
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
            @SerializedName("// Whether to check whole inventory when connecting / changing dimensions, etc.")
            public final String _comment_legaliseWholeInventory = "";
            public boolean legaliseWholeInventory = true;

            @SerializedName("// Which NBT shouldn't be cleared")
            public final String _comment_whitelistedNBT = "";
            /**
             * Clears NBT items, but still allows block-picking.
             */
            @SerializedName("// Disallow all NBT tags in creative which aren't in whitelist section.")
            public final String _comment_removeCreativeNBTTags = "";
            public ArrayList<String> whitelistedNBT = new ArrayList<>(Arrays.asList(
                    "EntityTag",
                    "Enchantments",
                    "StoredEnchantments",
                    "BlockEntityTag",
                    "Damage",
                    "Potion",
                    "display"
            ));
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

        /**
         * Whether to remove the teleport data
         * from packets when entities move out of
         * view distance.
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

        @SerializedName("// Allows health tags for certain entities.")
        public final String _comment_allowedHealthTags_1 = "";

        @SerializedName("// This maps entity ID to percentage as decimal.")
        public final String _comment_allowedHealthTags_2 = "";

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
        /**
         * Client can tell server its onGround status and
         * server blindly accepts it. This can allow
         * client to not take any fall damage.
         * This setting re-enables the check server-side
         * and doesn't care about the client's onGround status.
         */
        public boolean patchNoFall = true;
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

    public static class Duplication {
        public final String _comment = "// Duplication fixes";
        @SerializedName("// Whether to prevent throwing an error when saving large string data.")
        public final String _comment_patchSaveLimit1 = "";
        @SerializedName("// This is done by ignoring data after DataOutputStream limit.")
        public final String _comment_patchSaveLimit2 = "";
        @SerializedName("// Written books can reach that point with hacked clients.")
        public final String _comment_patchSaveLimit3 = "";
        public boolean patchSaveLimit = true;

        @SerializedName("// Whether to disable gravity block duping.")
        public final String _comment_patchGravityBlock = "";
        public boolean patchGravityBlock = true;
        @SerializedName("// Checks if player is connected before applying damage.")
        public final String _comment_patchDeathDuplication1 = "";
        @SerializedName("// Prevents duplicating inventory if player dies after disconnect.")
        public final String _comment_patchDeathDuplication2 = "";
        public boolean patchDeathDuplication = true;
    }
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
     * @author Ampflower
     */
    private static final class BlockSetAdapter extends TypeAdapter<Set<Block>> {

        @Override
        public void write(JsonWriter out, Set<Block> value) throws IOException {
            out.beginArray();
            var reg = Registries.BLOCK;
            for (var block : value) {
                out.value(reg.getId(block).toString());
            }
            out.endArray();
        }

        @Override
        public Set<Block> read(JsonReader in) throws IOException {
            in.beginArray();
            var reg = Registries.BLOCK;
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
     * @author Ampflower
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
