package kr.pyke.blockhider.game;

import kr.pyke.blockhider.config.ConfigParsers;
import kr.pyke.blockhider.config.ModConfig;
import kr.pyke.blockhider.data.BlockHiderSavedData;
import kr.pyke.blockhider.network.ModPackets;
import kr.pyke.blockhider.registry.item.ModItems;
import kr.pyke.blockhider.type.GAME_ROLE;
import kr.pyke.blockhider.type.GAME_STATE;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;

import java.util.List;

public class GameTimer {
    private static final int TICKS_PER_SECOND = 20;
    private static final int DEFAULT_BUFF_AMPLIFIER = 0;
    private static final int COUNTDOWN_DURATION = 5;
    private static final int[] RUNNING_ALERT_SECONDS = { 30, 10, 5, 4, 3, 2, 1 };
    private static final int[] PREPARING_ALERT_SECONDS = { 10 };
    private static final int PREPARING_COUNTDOWN_THRESHOLD = 5;
    private static final int TITLE_FADE_IN = 0;
    private static final int TITLE_STAY = 20;
    private static final int TITLE_FADE_OUT = 10;

    private final GameManager gameManager;
    private int tickCounter;
    private int remainingSeconds;
    private int totalSeconds;

    public GameTimer(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    public int getRemainingSeconds() { return this.remainingSeconds; }

    public int getTotalSeconds() { return this.totalSeconds; }

    public void startCountdown(MinecraftServer server) {
        this.totalSeconds = COUNTDOWN_DURATION;
        this.remainingSeconds = this.totalSeconds;
        this.tickCounter = 0;

        for (PlayerGameData playerGameData : this.gameManager.getData().getPlayers()) {
            ServerPlayer player = server.getPlayerList().getPlayer(playerGameData.getUUID());
            if (player == null) { continue; }

            player.getInventory().clearContent();
            player.setHealth(player.getMaxHealth());
        }

        applyTitleAnimation(server);
        broadcastTitle(server, Component.literal(String.valueOf(this.remainingSeconds)).withStyle(ChatFormatting.YELLOW));
    }

    public void tick(MinecraftServer server) {
        if (!this.gameManager.isRunning()) { return; }

        this.tickCounter++;
        if (this.tickCounter < TICKS_PER_SECOND) { return; }

        this.tickCounter = 0;
        this.remainingSeconds--;

        if (this.remainingSeconds <= 0) {
            this.onPhaseEnd(server);
        }
        else {
            this.onSecondTick(server);
        }

        ModPackets.broadcastGameState(server);
    }

    public void stop() {
        this.tickCounter = 0;
        this.remainingSeconds = 0;
        this.totalSeconds = 0;
    }

    private void onSecondTick(MinecraftServer server) {
        GAME_STATE state = this.gameManager.getData().getState();

        if (state == GAME_STATE.COUNTDOWN) {
            broadcastTitle(server, Component.literal(String.valueOf(this.remainingSeconds)).withStyle(ChatFormatting.YELLOW));
            broadcastSound(server, SoundEvents.UI_BUTTON_CLICK, SoundSource.MASTER, 1.f, 1.f);
            return;
        }

        if (state == GAME_STATE.PREPARING) {
            this.notifyPreparingAlert(server, this.remainingSeconds);
            return;
        }

        if (state == GAME_STATE.RUNNING) {
            this.applyMatchingPhase(server);
            this.notifyRunningAlert(server, this.remainingSeconds);
            this.broadcastSeekerActionBar(server);
        }
    }

    private void onPhaseEnd(MinecraftServer server) {
        GAME_STATE state = this.gameManager.getData().getState();
        if (state == GAME_STATE.COUNTDOWN) {
            this.startPreparation(server);
            return;
        }
        if (state == GAME_STATE.PREPARING) {
            this.startRunning(server);
            return;
        }

        this.gameManager.handleTimeUp(server);
    }

    private void startPreparation(MinecraftServer server) {
        this.gameManager.getData().setState(GAME_STATE.PREPARING);
        this.totalSeconds = ModConfig.getPreparationTimeSeconds();
        this.remainingSeconds = this.totalSeconds;

        for (PlayerGameData playerGameData : this.gameManager.getData().getPlayers()) {
            ServerPlayer player = server.getPlayerList().getPlayer(playerGameData.getUUID());
            if (player == null) { continue; }

            GameManager.getInstance().teleportToSpawn(player, BlockHiderSavedData.get(server));
            player.setGameMode(GameType.ADVENTURE);
            if (playerGameData.getRole() == GAME_ROLE.HIDER) {
                this.giveHiderItems(player);
            }
            else if (playerGameData.getRole() == GAME_ROLE.SEEKER) {
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, MobEffectInstance.INFINITE_DURATION, DEFAULT_BUFF_AMPLIFIER, false, false, true));
            }
        }
    }

    private void startRunning(MinecraftServer server) {
        this.gameManager.getData().setState(GAME_STATE.RUNNING);
        this.totalSeconds = ModConfig.getGameTimeSeconds();
        this.remainingSeconds = this.totalSeconds;

        for (PlayerGameData playerGameData : this.gameManager.getData().getPlayers()) {
            ServerPlayer player = server.getPlayerList().getPlayer(playerGameData.getUUID());
            if (player == null) { continue; }

            player.connection.send(new ClientboundSetTitleTextPacket(Component.literal("게임 시작").withStyle(ChatFormatting.RED)));
            player.connection.send(new ClientboundSoundPacket(SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(1), SoundSource.MASTER, player.getX(), player.getY(), player.getZ(), 1.f, 1.f, player.getRandom().nextLong()));
            if (playerGameData.getRole() == GAME_ROLE.SEEKER) {
                player.removeEffect(MobEffects.BLINDNESS);
                this.giveSeekerItems(player);
            }

            player.heal(player.getMaxHealth());
            player.getFoodData().setFoodLevel(20);
            player.getFoodData().setSaturation(0.f);
        }

        this.applyInitialPhase(server);
    }

    private void giveSeekerItems(ServerPlayer player) {
        this.giveItems(player, ModConfig.getSeekerItems());

        Inventory inventory = player.getInventory();

        inventory.setItem(0, new ItemStack(Items.DIAMOND_SWORD));
        inventory.setSelectedSlot(0);

        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.DIAMOND_CHESTPLATE));

        if (ModConfig.getHintItemCount() != 0) {
            inventory.setItem(8, new ItemStack(ModItems.HINT_ITEM));
        }
    }

    private void giveItems(ServerPlayer player, List<ModConfig.ItemEntry> items) {
        Inventory inventory = player.getInventory();

        for (ModConfig.ItemEntry entry : items) {
            ItemStack stack = ConfigParsers.toItemStack(entry);
            if (stack.isEmpty()) { continue; }

            inventory.add(stack);
        }
    }

    private void giveHiderItems(ServerPlayer player) {
        this.giveItems(player, ModConfig.getHiderItems());

        Inventory inventory = player.getInventory();

        inventory.setItem(0, new ItemStack(Items.DIAMOND_PICKAXE));
        inventory.setSelectedSlot(0);

        inventory.setItem(1, new ItemStack(Items.SNOWBALL, GameManager.snowballCount));
    }

    private void applyInitialPhase(MinecraftServer server) {
        if (!ModConfig.isBuffEnabled()) { return; }

        ModConfig.BuffPhase active = null;
        for (ModConfig.BuffPhase phase : ModConfig.getBuffPhases()) {
            if (phase.remainingTimeSeconds() > this.remainingSeconds) { continue; }
            if (active != null && phase.remainingTimeSeconds() <= active.remainingTimeSeconds()) { continue; }

            active = phase;
        }

        if (active != null) { this.applyPhaseToSeekers(server, active); }
    }

    private void applyMatchingPhase(MinecraftServer server) {
        if (!ModConfig.isBuffEnabled()) { return; }

        for (ModConfig.BuffPhase phase : ModConfig.getBuffPhases()) {
            if (phase.remainingTimeSeconds() != this.remainingSeconds) { continue; }

            this.applyPhaseToSeekers(server, phase);
        }
    }

    private void applyPhaseToSeekers(MinecraftServer server, ModConfig.BuffPhase phase) {
        for (PlayerGameData playerGameData : this.gameManager.getData().getPlayers()) {
            if (playerGameData.getRole() != GAME_ROLE.SEEKER || !playerGameData.isAlive()) { continue; }

            ServerPlayer player = server.getPlayerList().getPlayer(playerGameData.getUUID());
            if (player == null) { continue; }

            for (String spec : phase.effects()) {
                MobEffectInstance instance = ConfigParsers.toInfiniteEffect(spec, DEFAULT_BUFF_AMPLIFIER);
                if (instance != null) { player.addEffect(instance); }
            }
        }
    }

    private void notifyPreparingAlert(MinecraftServer server, int seconds) {
        if (seconds <= PREPARING_COUNTDOWN_THRESHOLD) {
            broadcastTitle(server, Component.literal(String.valueOf(seconds)).withStyle(ChatFormatting.YELLOW));
            broadcastSound(server, SoundEvents.UI_BUTTON_CLICK, SoundSource.MASTER, 1.f, 1.f);
            return;
        }

        for (int alertSecond : PREPARING_ALERT_SECONDS) {
            if (alertSecond != seconds) { continue; }

            broadcastMessage(server, Component.literal(seconds + "초 후 술래가 소환됩니다"));
            return;
        }
    }

    private void notifyRunningAlert(MinecraftServer server, int seconds) {
        for (int alertSecond : RUNNING_ALERT_SECONDS) {
            if (alertSecond != seconds) { continue; }

            broadcastTitle(server, Component.literal(seconds + "초 남았습니다").withStyle(ChatFormatting.RED));
            return;
        }
    }

    private void broadcastSeekerActionBar(MinecraftServer server) {
        int hintMax = ModConfig.getHintItemCount();
        int used = this.gameManager.getData().getHintUseCount();
        String text = hintMax < 0 ? "힌트: " + used + " / ∞" : "힌트: " + used + " / " + hintMax;
        Component component = Component.literal(text).withStyle(ChatFormatting.AQUA);

        for (PlayerGameData playerGameData : this.gameManager.getData().getPlayers()) {
            if (playerGameData.getRole() != GAME_ROLE.SEEKER || !playerGameData.isAlive()) { continue; }

            ServerPlayer player = server.getPlayerList().getPlayer(playerGameData.getUUID());
            if (player == null) { continue; }

            player.sendSystemMessage(component, true);
        }
    }

    private void applyTitleAnimation(MinecraftServer server) {
        ClientboundSetTitlesAnimationPacket packet = new ClientboundSetTitlesAnimationPacket(TITLE_FADE_IN, TITLE_STAY, TITLE_FADE_OUT);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.connection.send(packet);
        }
    }

    private void broadcastTitle(MinecraftServer server, Component title) {
        ClientboundSetTitleTextPacket packet = new ClientboundSetTitleTextPacket(title);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.connection.send(packet);
        }
    }

    private void broadcastMessage(MinecraftServer server, MutableComponent message) {
        server.getPlayerList().broadcastSystemMessage(message, false);
    }

    private void broadcastSound(MinecraftServer server, Holder<SoundEvent> soundEvents, SoundSource soundSource, float volume, float pitch) {
        for (PlayerGameData data : this.gameManager.getData().getPlayers()) {
            ServerPlayer player = server.getPlayerList().getPlayer(data.getUUID());
            if (player != null) {
                player.connection.send(new ClientboundSoundPacket(soundEvents, soundSource, player.getX(), player.getY(), player.getZ(), volume, pitch, player.getRandom().nextLong()));
            }
        }
    }
}