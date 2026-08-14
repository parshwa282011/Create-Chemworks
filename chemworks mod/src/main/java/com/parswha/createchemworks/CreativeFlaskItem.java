package com.parswha.createchemworks;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class CreativeFlaskItem extends Item {
    public CreativeFlaskItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            openClientScreen(hand);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private static void openClientScreen(InteractionHand hand) {
        try {
            Class<?> client = Class.forName("com.parswha.createchemworks.client.ClientEvents");
            client.getMethod("openFlaskScreen", InteractionHand.class).invoke(null, hand);
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException("Could not open the Creative Flask element picker", error);
        }
    }
}
