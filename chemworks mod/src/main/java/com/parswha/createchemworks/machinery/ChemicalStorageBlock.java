package com.parswha.createchemworks.machinery;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import com.parswha.createchemworks.ModBlockEntities;
import com.parswha.createchemworks.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.InteractionResult;
import net.neoforged.neoforge.network.PacketDistributor;
import com.parswha.createchemworks.chemistry.OpenChemicalTankPayload;

public class ChemicalStorageBlock extends BaseEntityBlock {
    public static final MapCodec<ChemicalStorageBlock> CODEC = simpleCodec(ChemicalStorageBlock::new);
    public ChemicalStorageBlock(Properties properties) { super(properties); }
    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new ChemicalStorageBlockEntity(pos, state); }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
            BlockPos pos, Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof ChemicalStorageBlockEntity storage)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (stack.is(ModItems.CREATIVE_FLASK.get()) && stack.has(DataComponents.CUSTOM_NAME)) {
            if (!level.isClientSide && storage.chemicalVolume().fill(stack.getHoverName().getString(), 1_000,
                    ChemicalVolume.AMBIENT_K) > 0) storage.setChanged();
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        if (stack.is(ModItems.FLASK.get()) && storage.chemicalVolume().amount() >= 1_000) {
            if (!level.isClientSide) {
                ItemStack filled = stack.copyWithCount(1);
                filled.set(DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal(storage.chemicalVolume().chemical()));
                stack.shrink(1);
                if (!player.addItem(filled)) player.drop(filled, false);
                storage.chemicalVolume().drain(1_000); storage.setChanged();
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof ChemicalStorageBlockEntity storage)
            PacketDistributor.sendToPlayer(serverPlayer, OpenChemicalTankPayload.from(pos, storage));
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.CHEMICAL_STORAGE.get(), ChemicalStorageBlockEntity::tick);
    }
}
