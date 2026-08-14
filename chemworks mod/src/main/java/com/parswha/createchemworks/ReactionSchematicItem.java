package com.parswha.createchemworks;

import com.parswha.createchemworks.chemistry.ReactionResultSnapshot;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.Optional;

public final class ReactionSchematicItem extends Item {
    private static final String ROOT = "CreateChemworksReaction";

    public ReactionSchematicItem(Properties properties) { super(properties); }

    public static ItemStack create(ReactionResultSnapshot result) {
        ItemStack stack = new ItemStack(ModItems.REACTION_SCHEMATIC.get());
        var root = new net.minecraft.nbt.CompoundTag();
        root.put(ROOT, result.toTag());
        CustomData.set(DataComponents.CUSTOM_DATA, stack, root);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Reaction Schematic: " + result.equation()));
        return stack;
    }

    public static Optional<ReactionResultSnapshot> read(ItemStack stack) {
        if (!stack.is(ModItems.REACTION_SCHEMATIC.get())) return Optional.empty();
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        var root = data.copyTag();
        return root.contains(ROOT) ? Optional.of(ReactionResultSnapshot.fromTag(root.getCompound(ROOT))) : Optional.empty();
    }
}
