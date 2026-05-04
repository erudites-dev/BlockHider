package kr.pyke.blockhider.registry.item.hint;

import kr.pyke.blockhider.game.GameManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

public class HintItem extends Item {
    public HintItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull InteractionResult use(@NonNull Level level, @NonNull Player player, @NonNull InteractionHand hand) {
        if (!(player instanceof ServerPlayer serverPlayer)) { return InteractionResult.PASS; }

        boolean used = GameManager.getInstance().tryUseHint(serverPlayer);
        if (!used) { return InteractionResult.FAIL; }

        return InteractionResult.SUCCESS;
    }
}
