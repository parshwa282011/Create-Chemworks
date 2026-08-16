package com.parswha.createchemworks.world;

import com.parswha.createchemworks.chemistry.ChemistryRules;
import com.parswha.createchemworks.client.Elements.ElementsData;
import com.parswha.createchemworks.integration.tetra.MaterializedMetalItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class ChaosMetalOreBlock extends Block {
    public ChaosMetalOreBlock(Properties properties) { super(properties); }

    @Override
    public void playerDestroy(net.minecraft.world.level.Level level, net.minecraft.world.entity.player.Player player,
            BlockPos pos, BlockState state, net.minecraft.world.level.block.entity.BlockEntity entity, ItemStack tool) {
        super.playerDestroy(level, player, pos, state, entity, tool);
        if (!(level instanceof ServerLevel server) || !(player instanceof ServerPlayer)
                || !player.hasCorrectToolForDrops(state)) return;
        var metals = ElementsData.all().stream().filter(ChemistryRules::isMetal).toList();
        var element = metals.get(server.random.nextInt(metals.size()));
        popResource(server, pos, MaterializedMetalItem.create(
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("create_chemworks", "element/" + element.number()),
                element.name()));
    }
}
