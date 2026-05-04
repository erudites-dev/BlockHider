package kr.pyke.blockhider.config;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ConfigParsers {
    private static final int EFFECT_ID_MIN_PARTS = 2;
    private static final int EFFECT_ID_AMPLIFIER_PART = 2;

    private ConfigParsers() { }

    public static ItemStack toItemStack(ModConfig.ItemEntry entry) {
        Identifier id = Identifier.parse(entry.itemID());
        Item item = BuiltInRegistries.ITEM.getValue(id);
        if (item == null) { return ItemStack.EMPTY; }

        return new ItemStack(item, entry.amount());
    }

    public static MobEffectInstance toInfiniteEffect(String spec, int amplifierFallback) {
        String[] parts = spec.split(":");
        if (parts.length < EFFECT_ID_MIN_PARTS) { return null; }

        Identifier id = Identifier.fromNamespaceAndPath(parts[0], parts[1]);
        MobEffect effect = BuiltInRegistries.MOB_EFFECT.getValue(id);
        if (effect == null) { return null; }

        int amplifier = parts.length > EFFECT_ID_AMPLIFIER_PART ? Integer.parseInt(parts[EFFECT_ID_AMPLIFIER_PART]) : amplifierFallback;
        Holder<MobEffect> holder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect);

        return new MobEffectInstance(holder, MobEffectInstance.INFINITE_DURATION, amplifier);
    }
}
