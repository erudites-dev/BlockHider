package kr.pyke.blockhider.registry.item;

import kr.pyke.blockhider.BlockHider;
import kr.pyke.blockhider.registry.block.ModBlocks;
import kr.pyke.blockhider.registry.item.hint.HintItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.function.Function;

public class ModItems {
    public static final Item HINT_ITEM = registerFactory("hint_item", HintItem::new);
    public static final Item GHOST_BLOCK_ITEM = register("ghost_block", ModBlocks.GHOST_BLOCK);

    private ModItems() { }

    private static Item registerFactory(String name, Function<Item.Properties, Item> factory) {
        ResourceKey<Item> resourceKey = key(name);
        Item.Properties properties = new Item.Properties().setId(resourceKey);

        return Registry.register(BuiltInRegistries.ITEM, resourceKey, factory.apply(properties));
    }

    private static Item register(String name, Block block) {
        ResourceKey<Item> resourceKey = key(name);
        BlockItem blockItem = new BlockItem(block, new Item.Properties().setId(resourceKey));

        return Registry.register(BuiltInRegistries.ITEM, resourceKey, blockItem);
    }

    private static ResourceKey<Item> key(String name) {
        return ResourceKey.create(Registries.ITEM, BlockHider.id(name));
    }

    public static void register() {
        BlockHider.LOGGER.info("Registering Mod Items for " + BlockHider.MOD_ID);
    }
}
