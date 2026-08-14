package com.parswha.createchemworks.integration.tetra;

import com.parswha.createchemworks.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.Optional;

/** One carrier item for an unbounded number of live element and alloy materials. */
public final class MaterializedMetalItem extends Item {
    private static final String MATERIAL = "TetraMaterial";
    public MaterializedMetalItem(Properties properties) { super(properties); }

    public static ItemStack create(ResourceLocation materialId, String displayName) {
        ItemStack stack = new ItemStack(ModItems.MATERIALIZED_METAL.get());
        mark(stack, materialId, displayName);
        return stack;
    }

    /** Creates consumable alloy samples which can be put directly in Tetra material slots. */
    public static ItemStack createFlasks(ResourceLocation materialId, String displayName, int count) {
        ItemStack stack = new ItemStack(ModItems.FLASK.get(), count);
        mark(stack, materialId, displayName);
        return stack;
    }

    static void mark(ItemStack stack, ResourceLocation materialId, String displayName) {
        CompoundTag tag = new CompoundTag(); tag.putString(MATERIAL, materialId.toString());
        CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(displayName));
    }

    public static Optional<ResourceLocation> materialId(ItemStack stack) {
        if (!stack.is(ModItems.MATERIALIZED_METAL.get()) && !stack.is(ModItems.FLASK.get())) return Optional.empty();
        return taggedMaterialId(stack);
    }

    static Optional<ResourceLocation> taggedMaterialId(ItemStack stack) {
        String id = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getString(MATERIAL);
        return Optional.ofNullable(ResourceLocation.tryParse(id));
    }
}
