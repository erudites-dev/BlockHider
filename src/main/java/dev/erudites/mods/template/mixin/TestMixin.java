package dev.erudites.mods.template.mixin;

import dev.erudites.mods.template.TemplateMod;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
abstract class TestMixin {

    @Inject(
        method = "<init>",
        at = @At("RETURN")
    )
    private void testInject(CallbackInfo ci) {
        TemplateMod.LOGGER.info("Minecraft initialized!");
    }
}
