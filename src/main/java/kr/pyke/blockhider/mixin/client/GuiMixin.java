package kr.pyke.blockhider.mixin.client;

import kr.pyke.blockhider.client.state.ClientGameState;
import kr.pyke.blockhider.transform.PlayerTransform;
import kr.pyke.blockhider.type.GAME_ROLE;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Gui.class)
public class GuiMixin {
    @ModifyVariable(method = "nextContextualInfoState", at = @At("STORE"), name = "canShowLocatorInfo")
    private boolean blockhider$hideLocatorForSeeker(boolean canShowLocatorInfo) {
        if (canShowLocatorInfo && ClientGameState.getRole().orElse(null) == GAME_ROLE.SEEKER) {
            return false;
        }

        return canShowLocatorInfo;
    }

    @Redirect(method = "extractCrosshair", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;crosshairPickEntity:Lnet/minecraft/world/entity/Entity;", opcode = Opcodes.GETFIELD))
    private Entity blockhider$hideAttackIndicator(Minecraft mc) {
        Entity target = mc.crosshairPickEntity;
        if (target instanceof Player player) {
            PlayerTransform transform = (PlayerTransform) player;
            if (transform.blockhider$getTransformedBlock() != null) {
                return null;
            }
        }

        return target;
    }
}