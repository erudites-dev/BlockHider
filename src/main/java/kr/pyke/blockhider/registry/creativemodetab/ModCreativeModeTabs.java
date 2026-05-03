package kr.pyke.blockhider.registry.creativemodetab;

import kr.pyke.blockhider.BlockHider;
import kr.pyke.blockhider.registry.item.ModItems;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModCreativeModeTabs {
    public static final ResourceKey<CreativeModeTab> CREATIVE_TAB_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, BlockHider.id("creative_tab"));

    public static final CreativeModeTab DEFAULT_CREATIVE_TAB = FabricCreativeModeTab.builder()
        .icon(() -> new ItemStack(ModItems.HINT_ITEM))
        .title(Component.translatable("itemGroup.blockhider.creative_tab"))
        .displayItems((params, output) -> {
            output.accept(ModItems.HINT_ITEM);
        })
        .build();

    private ModCreativeModeTabs() { }

    public static void register() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, CREATIVE_TAB_KEY, DEFAULT_CREATIVE_TAB);
    }
}
