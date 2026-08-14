package com.parswha.createchemworks.chemistry;

import com.parswha.createchemworks.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import javax.annotation.Nullable;
import net.neoforged.neoforge.network.PacketDistributor;
import java.util.ArrayList;

/** Stores configured flasks and tests every reactant subset of size 2–9 on a redstone rising edge. */
public final class ReactionTesterBlock extends Block implements EntityBlock {
    public ReactionTesterBlock(BlockBehaviour.Properties properties) {
        super(properties); registerDefaultState(stateDefinition.any().setValue(BlockStateProperties.POWERED, false));
    }
    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new ReactionTesterBlockEntity(pos, state); }

    @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(ModItems.CREATIVE_FLASK.get())) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!stack.has(DataComponents.CUSTOM_NAME)) {
            if (!level.isClientSide()) player.sendSystemMessage(Component.literal("Choose an element by right-clicking the Creative Flask first.").withStyle(ChatFormatting.YELLOW));
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof ReactionTesterBlockEntity tester) {
            String name = stack.getHoverName().getString();
            if (tester.add(stack, player.getAbilities().instabuild)) player.sendSystemMessage(Component.literal("Added " + name + " flask (" + tester.flasks().size() + "/" + ReactionTesterBlockEntity.CAPACITY + ")").withStyle(ChatFormatting.AQUA));
            else player.sendSystemMessage(Component.literal("Reaction Tester is full.").withStyle(ChatFormatting.RED));
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof ReactionTesterBlockEntity tester) {
            if (player.isShiftKeyDown()) {
                ItemStack removed = tester.removeLast();
                if (!removed.isEmpty() && !player.addItem(removed)) player.drop(removed, false);
            } else {
                if(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) PacketDistributor.sendToPlayer(serverPlayer,OpenTesterPayload.from(pos, tester));
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
    @Override protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        boolean powered = level.hasNeighborSignal(pos);
        if (powered == state.getValue(BlockStateProperties.POWERED)) return;
        level.setBlock(pos, state.setValue(BlockStateProperties.POWERED, powered), 3);
        if (powered && level instanceof ServerLevel server && level.getBlockEntity(pos) instanceof ReactionTesterBlockEntity tester) tester.runAll(server);
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(BlockStateProperties.POWERED); }
    @Override protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moving) {
        if (!state.is(next.getBlock()) && level.getBlockEntity(pos) instanceof ReactionTesterBlockEntity tester) tester.flasks().forEach(stack -> popResource(level, pos, stack));
        super.onRemove(state, level, pos, next, moving);
    }
}
