package com.parswha.createchemworks.machinery;

import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.foundation.block.IBE;
import com.parswha.createchemworks.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public final class ChemicalPumpBlock extends KineticBlock implements IBE<ChemicalPumpBlockEntity> {
    public ChemicalPumpBlock(Properties properties) { super(properties); }
    @Override public Direction.Axis getRotationAxis(BlockState state) { return Direction.Axis.Y; }
    @Override public boolean hasShaftTowards(LevelReader level, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == Direction.Axis.Y;
    }
    @Override public Class<ChemicalPumpBlockEntity> getBlockEntityClass() { return ChemicalPumpBlockEntity.class; }
    @Override public BlockEntityType<? extends ChemicalPumpBlockEntity> getBlockEntityType() { return ModBlockEntities.CHEMICAL_PUMP.get(); }
}
