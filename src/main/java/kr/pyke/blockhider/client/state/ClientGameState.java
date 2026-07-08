package kr.pyke.blockhider.client.state;

import kr.pyke.blockhider.type.GAME_ROLE;
import kr.pyke.blockhider.type.GAME_STATE;

import java.util.Optional;

public class ClientGameState {
    private static GAME_STATE state = GAME_STATE.WAITING;
    private static int remainingSeconds = 0;
    private static int totalSeconds = 0;
    private static Optional<GAME_ROLE> role = Optional.empty();
    private static int aliveSeekers = 0;
    private static int aliveHiders = 0;
    private static int hintUsed = 0;
    private static int hintMax = 0;

    private static String seekers = "";

    private ClientGameState() { }

    public static void update(GAME_STATE newState, int newRemainingSeconds, int newTotalSeconds, Optional<GAME_ROLE> newRole, int newAliveSeekers, int newAliveHiders, int newHintUsed, int newHintMax) {
        state = newState;
        remainingSeconds = newRemainingSeconds;
        totalSeconds = newTotalSeconds;
        role = newRole;
        aliveSeekers = newAliveSeekers;
        aliveHiders = newAliveHiders;
        hintUsed = newHintUsed;
        hintMax = newHintMax;
    }

    public static void update(String seekers) {
        ClientGameState.seekers = seekers;
    }

    public static GAME_STATE getState() { return state; }

    public static int getRemainingSeconds() { return remainingSeconds; }

    public static int getTotalSeconds() { return totalSeconds; }

    public static Optional<GAME_ROLE> getRole() { return role; }

    public static int getAliveSeekers() { return aliveSeekers; }

    public static int getAliveHiders() { return aliveHiders; }

    public static int getHintUsed() { return hintUsed; }

    public static int getHintMax() { return hintMax; }

    public static String getSeekers() { return seekers; }
}