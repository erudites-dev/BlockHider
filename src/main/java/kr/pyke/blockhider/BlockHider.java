package kr.pyke.blockhider;

import kr.pyke.blockhider.config.ModConfig;
import kr.pyke.blockhider.network.ModPackets;
import kr.pyke.blockhider.registry.creativemodetab.ModCreativeModeTabs;
import kr.pyke.blockhider.registry.item.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockHider implements ModInitializer {
    public static final String MOD_ID = "blockhider";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static MinecraftServer SERVER_INSTANCE;

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            SERVER_INSTANCE = server;
            ModConfig.load();
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> SERVER_INSTANCE = null);

        ModPackets.registerCodec();
        ModPackets.registerServer();

        ModItems.register();
        ModCreativeModeTabs.register();
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
