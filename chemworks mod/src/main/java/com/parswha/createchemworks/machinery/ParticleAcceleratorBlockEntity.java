package com.parswha.createchemworks.machinery;

import com.parswha.createchemworks.CreateChemworks;
import com.parswha.createchemworks.ModBlockEntities;
import com.parswha.createchemworks.client.Elements.ElectronBlock;
import com.parswha.createchemworks.client.Elements.Element;
import com.parswha.createchemworks.client.Elements.ElementsData;
import com.parswha.createchemworks.integration.tetra.MaterializedMetalItem;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public final class ParticleAcceleratorBlockEntity extends KineticBlockEntity {
    private static final List<Element> T_ELEMENTS = ElementsData.all().stream().filter(e -> e.block() == ElectronBlock.T).toList();
    private ItemStack input = ItemStack.EMPTY, output = ItemStack.EMPTY;
    private int progress;
    public ParticleAcceleratorBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.PARTICLE_ACCELERATOR.get(), pos, state); }
    public ItemStack input() { return input; }
    public ItemStack output() { return output; }
    public void insert(ItemStack stack) { if (input.isEmpty()) { input = stack; progress = 0; setChanged(); } }
    public ItemStack takeOutput() { ItemStack result = output; output = ItemStack.EMPTY; setChanged(); return result; }

    @Override public void tick() {
        super.tick();
        if (level == null || level.isClientSide || input.isEmpty() || !output.isEmpty()
                || isOverStressed() || Math.abs(getSpeed()) < 256) return;
        progress += Math.max(1, (int) Math.abs(getSpeed()) / 256);
        if (progress < 1_200) return;
        int index = Math.floorMod(worldPosition.hashCode() + (int) level.getGameTime(), T_ELEMENTS.size());
        Element element = T_ELEMENTS.get(index);
        output = MaterializedMetalItem.create(ResourceLocation.fromNamespaceAndPath(CreateChemworks.MOD_ID,
                "element/" + element.number()), element.name() + " ingot");
        input = ItemStack.EMPTY; progress = 0; setChanged(); sendData();
    }

    @Override protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        if (!input.isEmpty()) tag.put("input", input.save(registries));
        if (!output.isEmpty()) tag.put("output", output.save(registries));
        tag.putInt("progress", progress);
    }
    @Override protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        input = tag.contains("input") ? ItemStack.parseOptional(registries, tag.getCompound("input")) : ItemStack.EMPTY;
        output = tag.contains("output") ? ItemStack.parseOptional(registries, tag.getCompound("output")) : ItemStack.EMPTY;
        progress = tag.getInt("progress");
    }
}
