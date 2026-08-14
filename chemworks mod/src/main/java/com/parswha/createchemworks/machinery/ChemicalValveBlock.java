package com.parswha.createchemworks.machinery;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public final class ChemicalValveBlock extends ChemicalStorageBlock {
    public static final BooleanProperty OPEN = BooleanProperty.create("open");
    public static final MapCodec<ChemicalValveBlock> CODEC = simpleCodec(ChemicalValveBlock::new);
    public ChemicalValveBlock(Properties properties) { super(properties); registerDefaultState(stateDefinition.any().setValue(OPEN, true)); }
    @Override protected MapCodec<? extends ChemicalStorageBlock> codec() { return CODEC; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(OPEN); }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hit) {
        level.setBlock(pos, state.cycle(OPEN), Block.UPDATE_ALL);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
