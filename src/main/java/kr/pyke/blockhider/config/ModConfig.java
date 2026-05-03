package kr.pyke.blockhider.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import kr.pyke.blockhider.BlockHider;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModConfig {
    private static final String FILE_NAME = "blockhider.toml";
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);

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
        boolean fileExists = Files.exists(CONFIG_PATH);

        try (CommentedFileConfig config = CommentedFileConfig.builder(CONFIG_PATH).preserveInsertionOrder().sync().build()) {
            config.load();

            if (!fileExists) {
                writeDefaults(config);
                config.save();
            }

            seekerCount = config.getOrElse("game.seeker_count", DEFAULT_SEEKER_COUNT);
            hintItemCount = config.getOrElse("game.hint_item_count", DEFAULT_HINT_ITEM_COUNT);
            gameTimeSeconds = config.getOrElse("game.game_time_seconds", DEFAULT_GAME_TIME_SECONDS);
            preparationTimeSeconds = config.getOrElse("game.preparation_time_seconds", DEFAULT_PREP_TIME_SECONDS);

            seekerItems = readItemList(config, "seeker_items");
            hiderItems = readItemList(config, "hider_items");

            buffEnabled = config.getOrElse("seeker_buff.enabled", DEFAULT_BUFF_ENABLED);
            buffPhases = readBuffPhases(config);
        }
        catch (Exception e) {
            BlockHider.LOGGER.error("Failed to load config", e);
        }
    }

    public static void save() {
        try (CommentedFileConfig config = CommentedFileConfig.builder(CONFIG_PATH).preserveInsertionOrder().sync().build()) {
            config.load();
            config.set("game.seeker_count", seekerCount);
            config.set("game.hint_item_count", hintItemCount);
            config.set("game.game_time_seconds", gameTimeSeconds);
            config.set("game.preparation_time_seconds", preparationTimeSeconds);
            config.set("seeker_buff.enabled", buffEnabled);
            config.save();
        }
        catch (Exception e) {
            BlockHider.LOGGER.error("Failed to save config", e);
        }
    }

    private static void writeDefaults(CommentedFileConfig config) {
        config.set("game.seeker_count", DEFAULT_SEEKER_COUNT);
        config.set("game.hint_item_count", DEFAULT_HINT_ITEM_COUNT);
        config.set("game.game_time_seconds", DEFAULT_GAME_TIME_SECONDS);
        config.set("game.preparation_time_seconds", DEFAULT_PREP_TIME_SECONDS);
        config.set("seeker_items", new ArrayList<CommentedConfig>());
        config.set("hider_items", new ArrayList<CommentedConfig>());
        config.set("seeker_buff.enabled", DEFAULT_BUFF_ENABLED);
        config.set("seeker_buff.phases", new ArrayList<CommentedConfig>());
    }

    private static List<ItemEntry> readItemList(CommentedFileConfig config, String key) {
        List<CommentedConfig> raw = config.getOrElse(key, Collections.emptyList());
        List<ItemEntry> result = new ArrayList<>();
        for (CommentedConfig entry : raw) {
            String item = entry.getOrElse("item", "");
            int count = entry.getOrElse("count", 1);
            List<String> enchantments = entry.getOrElse("enchantments", Collections.emptyList());
            if (!item.isEmpty()) {
                result.add(new ItemEntry(item, count, enchantments));
            }
        }
        return result;
    }

    private static List<BuffPhase> readBuffPhases(CommentedFileConfig config) {
        List<CommentedConfig> raw = config.getOrElse("seeker_buff.phases", Collections.emptyList());
        List<BuffPhase> result = new ArrayList<>();
        for (CommentedConfig entry : raw) {
            int remaining = entry.getOrElse("remaining_time_seconds", 0);
            List<String> effects = entry.getOrElse("effects", Collections.emptyList());
            result.add(new BuffPhase(remaining, effects));
        }
        return result;
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


    public record ItemEntry(String itemID, int count, List<String> enchantments) { }
    public record BuffPhase(int remainingTimeSeconds, List<String> effects) { }
}
