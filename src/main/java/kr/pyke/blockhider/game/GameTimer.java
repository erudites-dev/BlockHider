package kr.pyke.blockhider.game;

import kr.pyke.blockhider.config.ConfigParsers;
import kr.pyke.blockhider.config.ModConfig;
import kr.pyke.blockhider.network.ModPackets;
import kr.pyke.blockhider.registry.item.ModItems;
import kr.pyke.blockhider.type.GAME_ROLE;
import kr.pyke.blockhider.type.GAME_STATE;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class GameTimer {
    private static final int TICKS_PER_SECOND = 20;
    private static final int DEFAULT_BUFF_AMPLIFIER = 0;
    private static final int COUNTDOWN_DURATION = 5;
    private static final int[] RUNNING_ALERT_SECONDS = {30, 10, 5, 4, 3, 2, 1};
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

        this.gameManager.stop(server);
    }

    private void startPreparation(MinecraftServer server) {
        this.gameManager.getData().setState(GAME_STATE.PREPARING);
        this.totalSeconds = ModConfig.getPreparationTimeSeconds();
        this.remainingSeconds = this.totalSeconds;

        for (PlayerGameData playerGameData : this.gameManager.getData().getPlayers()) {
            ServerPlayer player = server.getPlayerList().getPlayer(playerGameData.getUUID());
            if (player == null) { continue; }

            this.equipForPreparation(player, playerGameData);
        }
    }

    private void startRunning(MinecraftServer server) {
        this.gameManager.getData().setState(GAME_STATE.RUNNING);
        this.totalSeconds = ModConfig.getGameTimeSeconds();
        this.remainingSeconds = this.totalSeconds;

        for (PlayerGameData playerGameData : this.gameManager.getData().getPlayers()) {
            if (playerGameData.getRole() != GAME_ROLE.SEEKER) { continue; }

            ServerPlayer player = server.getPlayerList().getPlayer(playerGameData.getUUID());
            if (player == null) { continue; }

            player.removeEffect(MobEffects.BLINDNESS);
            this.giveSeekerItems(player);
        }

        this.applyInitialPhase(server);
    }

    private void equipForPreparation(ServerPlayer player, PlayerGameData playerGameData) {
        if (playerGameData.getRole() == GAME_ROLE.HIDER) {
            this.giveItems(player, ModConfig.getHiderItems());
            return;
        }

        if (playerGameData.getRole() == GAME_ROLE.SEEKER) {
            int durationTicks = ModConfig.getPreparationTimeSeconds() * TICKS_PER_SECOND;
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, durationTicks, DEFAULT_BUFF_AMPLIFIER, false, false, true));
        }
    }

    private void giveSeekerItems(ServerPlayer player) {
        this.giveItems(player, ModConfig.getSeekerItems());

        int hintCount = ModConfig.getHintItemCount();
        if (hintCount == 0) { return; }

        ItemStack hintStack = new ItemStack(ModItems.HINT_ITEM);
        player.getInventory().add(hintStack);
    }

    private void giveItems(ServerPlayer player, List<ModConfig.ItemEntry> entries) {
        for (ModConfig.ItemEntry entry : entries) {
            ItemStack stack = ConfigParsers.toItemStack(entry);
            if (stack.isEmpty()) { continue; }

            player.getInventory().add(stack);
        }
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
}