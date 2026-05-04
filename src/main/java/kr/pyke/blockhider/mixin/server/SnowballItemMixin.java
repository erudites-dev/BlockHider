package kr.pyke.blockhider.mixin.server;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.SnowballItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(SnowballItem.class)
public class SnowballItemMixin {
    @ModifyArg(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/Item;<init>(Lnet/minecraft/world/item/Item$Properties;)V"
        )
    )
    private static Item.Properties modifyMaxStackSize(Item.Properties properties) {
        return properties.stacksTo(99);
    }
}
