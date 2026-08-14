package com.parswha.createchemworks;

import com.parswha.createchemworks.chemistry.ReactionTesterBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import com.parswha.createchemworks.machinery.ChemicalStorageBlockEntity;
import com.parswha.createchemworks.machinery.ChemicalPumpBlockEntity;
import com.parswha.createchemworks.machinery.ReactionControllerBlockEntity;
import com.parswha.createchemworks.machinery.ParticleAcceleratorBlockEntity;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, CreateChemworks.MOD_ID);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ReactionTesterBlockEntity>> REACTION_TESTER = BLOCK_ENTITIES.register("reaction_tester", () -> BlockEntityType.Builder.of(ReactionTesterBlockEntity::new, ModBlocks.REACTION_TESTER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChemicalStorageBlockEntity>> CHEMICAL_STORAGE = BLOCK_ENTITIES.register("chemical_storage",
            () -> BlockEntityType.Builder.of(ChemicalStorageBlockEntity::new, ModBlocks.CHEMICAL_TANK.get(),
                    ModBlocks.CHEMICAL_PIPE.get(), ModBlocks.CHEMICAL_VALVE.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChemicalPumpBlockEntity>> CHEMICAL_PUMP = BLOCK_ENTITIES.register("chemical_pump",
            () -> BlockEntityType.Builder.of(ChemicalPumpBlockEntity::new, ModBlocks.CHEMICAL_PUMP.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ReactionControllerBlockEntity>> REACTION_CONTROLLER = BLOCK_ENTITIES.register("reaction_controller",
            () -> BlockEntityType.Builder.of(ReactionControllerBlockEntity::new, ModBlocks.REACTION_CONTROLLER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ParticleAcceleratorBlockEntity>> PARTICLE_ACCELERATOR = BLOCK_ENTITIES.register("particle_accelerator",
            () -> BlockEntityType.Builder.of(ParticleAcceleratorBlockEntity::new, ModBlocks.PARTICLE_ACCELERATOR.get()).build(null));
    private ModBlockEntities() { }
}
