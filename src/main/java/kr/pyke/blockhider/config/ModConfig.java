package kr.pyke.blockhider.config;

import com.google.gson.*;
import kr.pyke.blockhider.BlockHider;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModConfig {
    private static final String FILE_NAME = "blockhider.json";
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final int DEFAULT_SEEKER_COUNT = 1;
    private static final int DEFAULT_HINT_ITEM_COUNT = -1;
    private static final int DEFAULT_GAME_TIME_SECONDS = 300;
    private static final int DEFAULT_PREP_TIME_SECONDS = 60;
    private static final boolean DEFAULT_BUFF_ENABLED = true;

    private static int seekerCount = DEFAULT_SEEKER_COUNT;
    private static int hintItemCount = DEFAULT_HINT_ITEM_COUNT;
    private static int gameTimeSeconds = DEFAULT_GAME_TIME_SECONDS;
    private static int preparationTimeSeconds = DEFAULT_PREP_TIME_SECONDS;

    private static List<ItemEntry> seekerItems = new ArrayList<>();
    private static List<ItemEntry> hiderItems = new ArrayList<>();

    private static boolean buffEnabled = DEFAULT_BUFF_ENABLED;
    private static List<BuffPhase> buffPhases = new ArrayList<>();

    private ModConfig() { }

    public static void load() {
        try {
            if (!Files.exists(CONFIG_PATH)) {
                writeDefaultFile();
                return;
            }

            String text = Files.readString(CONFIG_PATH);
            JsonObject root = JsonParser.parseString(text).getAsJsonObject();

            JsonObject game = getObject(root, "game");
            seekerCount = getInt(game, "seeker_count", DEFAULT_SEEKER_COUNT);
            hintItemCount = getInt(game, "hint_item_count", DEFAULT_HINT_ITEM_COUNT);
            gameTimeSeconds = getInt(game, "game_time_seconds", DEFAULT_GAME_TIME_SECONDS);
            preparationTimeSeconds = getInt(game, "preparation_time_seconds", DEFAULT_PREP_TIME_SECONDS);

            seekerItems = readItemList(root, "seeker_items");
            hiderItems = readItemList(root, "hider_items");

            JsonObject buff = getObject(root, "seeker_buff");
            buffEnabled = getBoolean(buff, "enabled", DEFAULT_BUFF_ENABLED);
            buffPhases = readBuffPhases(buff);
        }
        catch (Exception e) {
            BlockHider.LOGGER.error("Failed to load config", e);
        }
    }

    public static void save() {
        try {
            JsonObject root = new JsonObject();

            JsonObject game = new JsonObject();
            game.addProperty("seeker_count", seekerCount);
            game.addProperty("hint_item_count", hintItemCount);
            game.addProperty("game_time_seconds", gameTimeSeconds);
            game.addProperty("preparation_time_seconds", preparationTimeSeconds);
            root.add("game", game);

            root.add("seeker_items", writeItemList(seekerItems));
            root.add("hider_items", writeItemList(hiderItems));

            JsonObject buff = new JsonObject();
            buff.addProperty("enabled", buffEnabled);
            buff.add("phases", writeBuffPhases(buffPhases));
            root.add("seeker_buff", buff);

            Files.writeString(CONFIG_PATH, GSON.toJson(root));
        }
        catch (Exception e) {
            BlockHider.LOGGER.error("Failed to save config", e);
        }
    }

    private static void writeDefaultFile() {
        save();
    }

    private static JsonObject getObject(JsonObject root, String key) {
        if (!root.has(key) || !root.get(key).isJsonObject()) { return new JsonObject(); }
        return root.getAsJsonObject(key);
    }

    private static int getInt(JsonObject obj, String key, int fallback) {
        if (!obj.has(key) || !obj.get(key).isJsonPrimitive()) { return fallback; }
        return obj.get(key).getAsInt();
    }

    private static boolean getBoolean(JsonObject obj, String key, boolean fallback) {
        if (!obj.has(key) || !obj.get(key).isJsonPrimitive()) { return fallback; }
        return obj.get(key).getAsBoolean();
    }

    private static String getString(JsonObject obj, String key, String fallback) {
        if (!obj.has(key) || !obj.get(key).isJsonPrimitive()) { return fallback; }
        return obj.get(key).getAsString();
    }

    private static List<ItemEntry> readItemList(JsonObject root, String key) {
        List<ItemEntry> result = new ArrayList<>();
        if (!root.has(key) || !root.get(key).isJsonArray()) { return result; }

        for (JsonElement element : root.getAsJsonArray(key)) {
            if (!element.isJsonObject()) { continue; }

            JsonObject entry = element.getAsJsonObject();
            String item = getString(entry, "item", "");
            int amount = getInt(entry, "amount", 1);
            List<String> enchantments = readStringArray(entry, "enchantments");
            if (item.isEmpty()) { continue; }

            result.add(new ItemEntry(item, amount, enchantments));
        }
        return result;
    }

    private static List<BuffPhase> readBuffPhases(JsonObject buff) {
        List<BuffPhase> result = new ArrayList<>();
        if (!buff.has("phases") || !buff.get("phases").isJsonArray()) { return result; }

        for (JsonElement element : buff.getAsJsonArray("phases")) {
            if (!element.isJsonObject()) { continue; }

            JsonObject entry = element.getAsJsonObject();
            int remaining = getInt(entry, "remaining_time_seconds", 0);
            List<String> effects = readStringArray(entry, "effects");
            result.add(new BuffPhase(remaining, effects));
        }
        return result;
    }

    private static List<String> readStringArray(JsonObject obj, String key) {
        List<String> result = new ArrayList<>();
        if (!obj.has(key) || !obj.get(key).isJsonArray()) { return result; }

        for (JsonElement element : obj.getAsJsonArray(key)) {
            if (element.isJsonPrimitive()) { result.add(element.getAsString()); }
        }
        return result;
    }

    private static JsonArray writeItemList(List<ItemEntry> items) {
        JsonArray array = new JsonArray();
        for (ItemEntry entry : items) {
            JsonObject obj = new JsonObject();
            obj.addProperty("item", entry.itemID());
            obj.addProperty("amount", entry.amount());

            JsonArray enchArray = new JsonArray();
            for (String enchantment : entry.enchantments()) { enchArray.add(enchantment); }
            obj.add("enchantments", enchArray);

            array.add(obj);
        }
        return array;
    }

    private static JsonArray writeBuffPhases(List<BuffPhase> phases) {
        JsonArray array = new JsonArray();
        for (BuffPhase phase : phases) {
            JsonObject obj = new JsonObject();
            obj.addProperty("remaining_time_seconds", phase.remainingTimeSeconds());

            JsonArray effects = new JsonArray();
            for (String effect : phase.effects()) { effects.add(effect); }
            obj.add("effects", effects);

            array.add(obj);
        }
        return array;
    }

    public static int getSeekerCount() { return seekerCount; }
    public static int getHintItemCount() { return hintItemCount; }
    public static int getGameTimeSeconds() { return gameTimeSeconds; }
    public static int getPreparationTimeSeconds() { return preparationTimeSeconds; }
    public static List<ItemEntry> getSeekerItems() { return Collections.unmodifiableList(seekerItems); }
    public static List<ItemEntry> getHiderItems() { return Collections.unmodifiableList(hiderItems); }
    public static boolean isBuffEnabled() { return buffEnabled; }
    public static List<BuffPhase> getBuffPhases() { return Collections.unmodifiableList(buffPhases); }

    public static void setSeekerCount(int value) { seekerCount = value; }
    public static void setHintItemCount(int value) { hintItemCount = value; }
    public static void setGameTimeSeconds(int value) { gameTimeSeconds = value; }
    public static void setPreparationTimeSeconds(int value) { preparationTimeSeconds = value; }


    public record ItemEntry(String itemID, int amount, List<String> enchantments) { }
    public record BuffPhase(int remainingTimeSeconds, List<String> effects) { }
}