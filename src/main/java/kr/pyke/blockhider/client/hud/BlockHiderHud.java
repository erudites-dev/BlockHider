package kr.pyke.blockhider.client.hud;

import kr.pyke.blockhider.client.state.ClientGameState;
import kr.pyke.blockhider.type.GAME_ROLE;
import kr.pyke.blockhider.type.GAME_STATE;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public class BlockHiderHud implements HudElement {
    private static final int PADDING_X = 4;
    private static final int PADDING_Y = 4;
    private static final int LINE_HEIGHT = 10;
    private static final int COLOR_TITLE = 0xFFFFFF55;
    private static final int COLOR_TEXT = 0xFFFFFFFF;
    private static final int COLOR_SEEKER = 0xFFFF5555;
    private static final int COLOR_HIDER = 0xFF55FF55;
    private static final int COLOR_SPECTATOR = 0xFFAAAAAA;
    private static final int SECONDS_PER_MINUTE = 60;

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, @NonNull DeltaTracker deltaTracker) {
        GAME_STATE state = ClientGameState.getState();
        if (state == GAME_STATE.WAITING) { return; }

        Font font = Minecraft.getInstance().font;
        int line = 0;

        graphics.text(font, Component.literal(stateLabel(state)), PADDING_X, PADDING_Y, COLOR_TITLE);
        line++;

        graphics.text(font, Component.literal("시간: " + formatTime(ClientGameState.getRemainingSeconds())), PADDING_X, PADDING_Y + line * LINE_HEIGHT, COLOR_TEXT);
        line++;

        graphics.text(font, Component.literal("술래 " + ClientGameState.getAliveSeekers() + " / 숨은 사람 " + ClientGameState.getAliveHiders()), PADDING_X, PADDING_Y + line * LINE_HEIGHT, COLOR_TEXT);
        line++;

        Optional<GAME_ROLE> role = ClientGameState.getRole();
        if (role.isEmpty()) { return; }

        GAME_ROLE current = role.get();
        graphics.text(font, Component.literal("당신: " + roleName(current)), PADDING_X, PADDING_Y + line * LINE_HEIGHT, roleColor(current));
        line++;

        if (current != GAME_ROLE.SEEKER) { return; }

        int hintMax = ClientGameState.getHintMax();
        int hintUsed = ClientGameState.getHintUsed();
        String hintText = hintMax < 0 ? "힌트: " + hintUsed + " / ∞" : "힌트: " + hintUsed + " / " + hintMax;
        graphics.text(font, Component.literal(hintText), PADDING_X, PADDING_Y + line * LINE_HEIGHT, COLOR_TEXT);
    }

    private String stateLabel(GAME_STATE state) {
        if (state == GAME_STATE.PREPARING) { return "[숨바꼭질] 준비 시간"; }
        if (state == GAME_STATE.RUNNING) { return "[숨바꼭질] 진행 중"; }

        return "[숨바꼭질]";
    }

    private String roleName(GAME_ROLE role) {
        if (role == GAME_ROLE.SEEKER) { return "술래"; }
        if (role == GAME_ROLE.HIDER) { return "숨은 사람"; }

        return "관전자";
    }

    private int roleColor(GAME_ROLE role) {
        if (role == GAME_ROLE.SEEKER) { return COLOR_SEEKER; }
        if (role == GAME_ROLE.HIDER) { return COLOR_HIDER; }

        return COLOR_SPECTATOR;
    }

    private String formatTime(int seconds) {
        int minutes = seconds / SECONDS_PER_MINUTE;
        int remainder = seconds % SECONDS_PER_MINUTE;
        return String.format("%d:%02d", minutes, remainder);
    }
}
