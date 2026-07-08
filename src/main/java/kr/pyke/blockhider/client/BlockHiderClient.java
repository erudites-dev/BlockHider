package kr.pyke.blockhider.client;

import kr.pyke.blockhider.BlockHider;
import kr.pyke.blockhider.client.hud.BlockHiderHud;
import kr.pyke.blockhider.client.model.HintItemModel;
import kr.pyke.blockhider.network.ModPackets;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.resources.Identifier;

public class BlockHiderClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModelLoadingPlugin.register(pluginContext -> {
            pluginContext.modifyItemModelAfterBake().register(ModelModifier.WRAP_PHASE, (model, context) -> {
                Identifier targetID = BlockHider.id("hint_item");
                if (context.itemId().equals(targetID)) { return new HintItemModel(model); }

                return model;
            });
        });

        ModPackets.registerClient();

        HudElementRegistry.addLast(BlockHider.id("game_state"), new BlockHiderHud());
    }
}
