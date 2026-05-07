package kr.pyke.blockhider.mixin.client;

import kr.pyke.blockhider.client.state.ClientGameState;
import kr.pyke.blockhider.type.GAME_ROLE;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Gui.class)
public class GuiMixin {
    @ModifyVariable(method = "nextContextualInfoState", at = @At("STORE"), name = "canShowLocatorInfo")
    private boolean blockhider$hideLocatorForSeeker(boolean canShowLocatorInfo) {
        if (canShowLocatorInfo && ClientGameState.getRole().orElse(null) == GAME_ROLE.SEEKER) { return false; }

        return canShowLocatorInfo;
    }
}
