package com.parswha.createchemworks.machinery;

import com.parswha.createchemworks.ModBlockEntities;
import com.parswha.createchemworks.ModBlocks;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class ParticleAcceleratorBlock extends KineticBlock implements IBE<ParticleAcceleratorBlockEntity> {
    public ParticleAcceleratorBlock(Properties properties) { super(properties); }
    @Override public Direction.Axis getRotationAxis(BlockState state) { return Direction.Axis.Y; }
    @Override public boolean hasShaftTowards(LevelReader level, BlockPos pos, BlockState state, Direction face) { return face.getAxis().isVertical(); }
    @Override public Class<ParticleAcceleratorBlockEntity> getBlockEntityClass() { return ParticleAcceleratorBlockEntity.class; }
    @Override public BlockEntityType<? extends ParticleAcceleratorBlockEntity> getBlockEntityType() { return ModBlockEntities.PARTICLE_ACCELERATOR.get(); }
    @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
            BlockPos pos, Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof ParticleAcceleratorBlockEntity accelerator)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!accelerator.output().isEmpty()) {
            if (!level.isClientSide) player.addItem(accelerator.takeOutput());
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        if (stack.is(ModBlocks.CHAOS_STONE.asItem()) && accelerator.input().isEmpty()) {
            if (!level.isClientSide) { accelerator.insert(stack.copyWithCount(1)); stack.shrink(1); }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
}
