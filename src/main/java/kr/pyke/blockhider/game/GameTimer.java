package kr.pyke.blockhider.game;

import kr.pyke.blockhider.config.ConfigParsers;
import kr.pyke.blockhider.config.ModConfig;
import kr.pyke.blockhider.network.ModPackets;
import kr.pyke.blockhider.registry.item.ModItems;
import kr.pyke.blockhider.type.GAME_ROLE;
import kr.pyke.blockhider.type.GAME_STATE;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class GameTimer {
    private static final int TICKS_PER_SECOND = 20;
    private static final int DEFAULT_BUFF_AMPLIFIER = 0;

    private final GameManager gameManager;
    private int tickCounter;
    private int remainingSeconds;
    private int totalSeconds;

    public GameTimer(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    public int getRemainingSeconds() { return this.remainingSeconds; }

    public int getTotalSeconds() { return this.totalSeconds; }

    public void startPreparation(MinecraftServer server) {
        this.totalSeconds = ModConfig.getPreparationTimeSeconds();
        this.remainingSeconds = this.totalSeconds;
        this.tickCounter = 0;

        for (PlayerGameData playerGameData : this.gameManager.getData().getPlayers()) {
            ServerPlayer player = server.getPlayerList().getPlayer(playerGameData.getUUID());
            if (player == null) { continue; }

            this.equipForPreparation(player, playerGameData);
        }
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
        else if (this.gameManager.getData().getState() == GAME_STATE.RUNNING) {
            this.applyMatchingPhase(server);
        }

        ModPackets.broadcastGameState(server);
    }

    public void stop() {
        this.tickCounter = 0;
        this.remainingSeconds = 0;
        this.totalSeconds = 0;
    }

    private void onPhaseEnd(MinecraftServer server) {
        if (this.gameManager.getData().getState() == GAME_STATE.PREPARING) {
            this.startRunning(server);
            return;
        }

        this.gameManager.stop(server);
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
            this.giveHiderItems(player);
            return;
        }

        if (playerGameData.getRole() == GAME_ROLE.SEEKER) {
            int durationTicks = ModConfig.getPreparationTimeSeconds() * TICKS_PER_SECOND;
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, durationTicks, DEFAULT_BUFF_AMPLIFIER, false, false, true));
        }
    }

    private void giveSeekerItems(ServerPlayer player) {
        ItemStack swordItem = new ItemStack(Items.DIAMOND_SWORD);
        player.getInventory().setItem(0, swordItem);
        player.getInventory().setSelectedSlot(0);

        ItemStack armorItem = new ItemStack(Items.DIAMOND_CHESTPLATE);
        player.getInventory().setItem(Inventory.SLOT_BODY_ARMOR, armorItem);

        if (ModConfig.getHintItemCount() != 0) {
            ItemStack hintItem = new ItemStack(ModItems.HINT_ITEM);
            player.getInventory().setItem(8, hintItem);
        }

        this.giveItems(player, ModConfig.getSeekerItems());
    }

    private void giveHiderItems(ServerPlayer player) {
        ItemStack pickaxeItem = new ItemStack(Items.DIAMOND_PICKAXE);
        player.getInventory().setItem(0, pickaxeItem);
        player.getInventory().setSelectedSlot(0);

        ItemStack throwItem = new ItemStack(Items.SNOWBALL, 99);
        player.getInventory().setItem(8, throwItem);

        this.giveItems(player, ModConfig.getHiderItems());
    }

    private void giveItems(ServerPlayer player, List<ModConfig.ItemEntry> entries) {
        for (ModConfig.ItemEntry entry : entries) {
            ItemStack itemStack = ConfigParsers.toItemStack(entry);
            if (itemStack.isEmpty()) { continue; }

            player.getInventory().add(itemStack);
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
}
