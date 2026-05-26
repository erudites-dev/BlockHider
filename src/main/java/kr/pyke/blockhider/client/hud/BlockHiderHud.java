package kr.pyke.blockhider.client.hud;

import kr.pyke.blockhider.client.state.ClientGameState;
import kr.pyke.blockhider.type.GAME_STATE;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

public class BlockHiderHud implements HudElement {
    private static final int PADDING_X = 8;
    private static final int PADDING_Y = 8;
    private static final float TIME_SCALE = 2.f;
    private static final int LINE_GAP = 2;
    private static final int TOP_CENTER_Y = 4;
    private static final int COLOR_TIME = 0xFFFFFFFF;
    private static final int COLOR_STATE = 0xFFAAAAAA;
    private static final int COLOR_TEXT = 0xFFFFFFFF;
    private static final int SECONDS_PER_MINUTE = 60;

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, @NonNull DeltaTracker deltaTracker) {
        GAME_STATE state = ClientGameState.getState();
        if (state == GAME_STATE.WAITING) { return; }

        Font font = Minecraft.getInstance().font;

        String timeStr = formatTime(ClientGameState.getRemainingSeconds());
        graphics.pose().pushMatrix();
        graphics.pose().translate(PADDING_X, PADDING_Y);
        graphics.pose().scale(TIME_SCALE, TIME_SCALE);
        graphics.text(font, Component.literal(timeStr), 0, 0, COLOR_TIME);
        graphics.pose().popMatrix();

        int stateY = PADDING_Y + (int)(font.lineHeight * TIME_SCALE) + LINE_GAP;
        graphics.text(font, Component.literal(stateLabel(state)), PADDING_X, stateY, COLOR_STATE);

        int centerX = graphics.guiWidth() / 2;
        Component countComp = Component.literal("술래 " + ClientGameState.getAliveSeekers() + " | 숨은 사람 " + ClientGameState.getAliveHiders());
        graphics.centeredText(font, countComp, centerX, TOP_CENTER_Y, COLOR_TEXT);
    }

    private String stateLabel(GAME_STATE state) {
        if (state == GAME_STATE.RUNNING) { return "진행 중"; }
        else if (state == GAME_STATE.PREPARING) { return "대기"; }

        return "";
    }

    private String formatTime(int seconds) {
        int minutes = seconds / SECONDS_PER_MINUTE;
        int remainder = seconds % SECONDS_PER_MINUTE;
        return String.format("%02d:%02d", minutes, remainder);
    }
}