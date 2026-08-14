package com.parswha.createchemworks;

import com.parswha.createchemworks.chemistry.ReactionTesterBlock;
import com.parswha.createchemworks.chemistry.ReactionBoardBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import com.parswha.createchemworks.machinery.ChemicalStorageBlock;
import com.parswha.createchemworks.machinery.ChemicalValveBlock;
import com.parswha.createchemworks.machinery.ChemicalPumpBlock;
import com.parswha.createchemworks.machinery.ReactionControllerBlock;
import com.parswha.createchemworks.machinery.ParticleAcceleratorBlock;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(CreateChemworks.MOD_ID);
    public static final DeferredBlock<Block> REACTION_TESTER = BLOCKS.registerBlock("reaction_tester",
            ReactionTesterBlock::new, BlockBehaviour.Properties.of().strength(2.5f));
    public static final DeferredBlock<Block> REACTION_BOARD = BLOCKS.registerBlock("reaction_board",
            ReactionBoardBlock::new, BlockBehaviour.Properties.of().strength(2.0f));
    public static final DeferredBlock<ChemicalStorageBlock> CHEMICAL_TANK = BLOCKS.registerBlock("chemical_tank",
            ChemicalStorageBlock::new, BlockBehaviour.Properties.of().strength(4.0f));
    public static final DeferredBlock<ChemicalStorageBlock> CHEMICAL_PIPE = BLOCKS.registerBlock("chemical_pipe",
            ChemicalStorageBlock::new, BlockBehaviour.Properties.of().strength(3.0f));
    public static final DeferredBlock<ChemicalValveBlock> CHEMICAL_VALVE = BLOCKS.registerBlock("chemical_valve",
            ChemicalValveBlock::new, BlockBehaviour.Properties.of().strength(3.0f));
    public static final DeferredBlock<ChemicalPumpBlock> CHEMICAL_PUMP = BLOCKS.registerBlock("chemical_pump",
            ChemicalPumpBlock::new, BlockBehaviour.Properties.of().strength(4.0f));
    public static final DeferredBlock<ReactionControllerBlock> REACTION_CONTROLLER = BLOCKS.registerBlock("reaction_controller",
            ReactionControllerBlock::new, BlockBehaviour.Properties.of().strength(4.0f));
    public static final DeferredBlock<Block> CHAOS_STONE = BLOCKS.registerSimpleBlock("chaos_stone",
            BlockBehaviour.Properties.of().strength(50.0f, 1_200.0f).requiresCorrectToolForDrops());
    public static final DeferredBlock<Block> T_METAL_ORE = BLOCKS.registerSimpleBlock("t_metal_ore",
            BlockBehaviour.Properties.of().strength(65.0f, 1_200.0f).requiresCorrectToolForDrops());
    public static final DeferredBlock<ParticleAcceleratorBlock> PARTICLE_ACCELERATOR = BLOCKS.registerBlock("particle_accelerator",
            ParticleAcceleratorBlock::new, BlockBehaviour.Properties.of().strength(8.0f));

    private ModBlocks() { }
}
