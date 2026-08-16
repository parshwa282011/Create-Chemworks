package com.parswha.createchemworks.machinery;

import com.parswha.createchemworks.ModBlockEntities;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class ChemicalPumpBlockEntity extends KineticBlockEntity {
    public ChemicalPumpBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.CHEMICAL_PUMP.get(), pos, state); }

    @Override public void tick() {
        super.tick();
        if (level == null || level.isClientSide || isOverStressed() || Math.abs(getSpeed()) < 1) return;
        Direction positiveDirection = getBlockState().getValue(ChemicalPumpBlock.FACING);
        // The arrow is the positive-RPM direction; reversing the shaft reverses flow.
        Direction outputDirection = getSpeed() >= 0 ? positiveDirection : positiveDirection.getOpposite();
        BlockEntity sourceEntity = level.getBlockEntity(worldPosition.relative(outputDirection.getOpposite()));
        BlockEntity targetEntity = level.getBlockEntity(worldPosition.relative(outputDirection));
        if (!(sourceEntity instanceof ChemicalNode source) || !(targetEntity instanceof ChemicalNode target)
                || !source.permitsFlow() || !target.permitsFlow()) return;
        long rate = Math.max(1, Math.min(4_000, (long) Math.abs(getSpeed()) * 4));
        long moved = source.chemicalVolume().transferTo(target.chemicalVolume(), rate);
        if (moved > 0) {
            sourceEntity.setChanged(); targetEntity.setChanged();
        }
    }
}
