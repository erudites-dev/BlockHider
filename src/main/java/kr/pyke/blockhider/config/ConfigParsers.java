package kr.pyke.blockhider.config;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.List;

public class ConfigParsers {
    private static final int ID_MIN_PARTS = 2;
    private static final int ID_VALUE_PART = 2;

    private ConfigParsers() { }

    public static ItemStack toItemStack(ModConfig.ItemEntry entry, HolderLookup.Provider registries) {
        Identifier id = Identifier.parse(entry.itemID());
        Item item = BuiltInRegistries.ITEM.getValue(id);
        if (item == null) { return ItemStack.EMPTY; }

        ItemStack stack = new ItemStack(item, entry.amount());
        applyEnchantments(stack, entry.enchantments(), registries);

        return stack;
    }

    private static void applyEnchantments(ItemStack stack, List<String> enchantments, HolderLookup.Provider registries) {
        HolderLookup.RegistryLookup<Enchantment> lookup = registries.lookupOrThrow(Registries.ENCHANTMENT);

        for (String spec : enchantments) {
            String[] parts = spec.split(":");
            if (parts.length < ID_MIN_PARTS) { continue; }

            Identifier enchantID = Identifier.fromNamespaceAndPath(parts[0], parts[1]);
            int level = parts.length > ID_VALUE_PART ? Integer.parseInt(parts[ID_VALUE_PART]) : 1;

            ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT, enchantID);
            lookup.get(key).ifPresent(holder -> stack.enchant(holder, level));
        }
    }

    public static MobEffectInstance toInfiniteEffect(String spec, int amplifierFallback) {
        String[] parts = spec.split(":");
        if (parts.length < ID_MIN_PARTS) { return null; }

        Identifier id = Identifier.fromNamespaceAndPath(parts[0], parts[1]);
        MobEffect effect = BuiltInRegistries.MOB_EFFECT.getValue(id);
        if (effect == null) { return null; }

        int amplifier = parts.length > ID_VALUE_PART ? Integer.parseInt(parts[ID_VALUE_PART]) : amplifierFallback;
        Holder<MobEffect> holder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect);

        return new MobEffectInstance(holder, MobEffectInstance.INFINITE_DURATION, amplifier);
    }
}
