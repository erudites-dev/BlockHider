package kr.pyke.blockhider.client.model;

import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBakedItemModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NonNull;

public class HintItemModel extends WrapperBakedItemModel {
    public HintItemModel(ItemModel wrapped) {
        super(wrapped);
    }

    @Override
    public void update(@NonNull ItemStackRenderState renderState, @NonNull ItemStack itemStack, @NonNull ItemModelResolver resolver, @NonNull ItemDisplayContext displayContext, ClientLevel level, ItemOwner itemOwner, int seed) {
        if (displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND || displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || displayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND || displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND) {
            ItemStack fireworkStack = new ItemStack(Items.FIREWORK_ROCKET);
            resolver.updateForTopItem(renderState, fireworkStack, displayContext, level, itemOwner, seed);
        }
        else {
            super.update(renderState, itemStack, resolver, displayContext, level, itemOwner, seed);
        }
    }
}