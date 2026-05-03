package kr.pyke.blockhider.registry.item;

import kr.pyke.blockhider.BlockHider;
import kr.pyke.blockhider.registry.item.hint.HintItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

import java.util.function.Function;

public class ModItems {
    public static final Item HINT_ITEM = registerFactory("hint_item", HintItem::new);

    private ModItems() { }

    private static Item registerFactory(String name, Function<Item.Properties, Item> factory) {
        ResourceKey<Item> resourceKey = key(name);
        Item.Properties properties = new Item.Properties().setId(resourceKey);

        return Registry.register(BuiltInRegistries.ITEM, resourceKey, factory.apply(properties));
    }

    private static ResourceKey<Item> key(String name) {
        return ResourceKey.create(Registries.ITEM, BlockHider.id(name));
    }

    public static void register() {
        BlockHider.LOGGER.info("Registering Mod Items for " + BlockHider.MOD_ID);
    }
}
