package kr.pyke.blockhider.game;

import kr.pyke.blockhider.type.GAME_ROLE;
import kr.pyke.blockhider.type.GAME_STATE;

import java.util.*;

public class GameData {
    private GAME_STATE state = GAME_STATE.WAITING;
    private final Map<UUID, PlayerGameData> players = new HashMap<>();
    private int hintUseCount = 0;

    private boolean debugMode = false;

    public GAME_STATE getState() { return this.state; }

    public void setState(GAME_STATE state) { this.state = state; }

    public PlayerGameData getPlayerData(UUID uuid) { return this.players.get(uuid); }

    public Collection<PlayerGameData> getPlayers() { return Collections.unmodifiableCollection(this.players.values()); }

    public void addPlayer(PlayerGameData data) { this.players.put(data.getUUID(), data); }

    public void clearPlayers() { this.players.clear(); }

    public int getHintUseCount() { return this.hintUseCount; }

    public void setHintUseCount(int count) { this.hintUseCount = count; }

    public void incrementHintUseCount() { this.hintUseCount++; }

    public int getAliveCount(GAME_ROLE role) {
        int count = 0;
        for (PlayerGameData data : players.values()) {
            if (data.isAlive() && data.getRole() == role) {
                count++;
            }
        }

        return count;
    }

    public boolean isDebugMode() { return this.debugMode; }

    public void setDebugMode(boolean debugMode) { this.debugMode = debugMode; }
}
