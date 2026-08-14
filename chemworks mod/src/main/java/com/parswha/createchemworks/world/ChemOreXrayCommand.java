package com.parswha.createchemworks.world;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.parswha.createchemworks.CreateChemworks;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Developer ore scanner. It searches existing loaded chunks without generating new terrain. */
@EventBusSubscriber(modid = CreateChemworks.MOD_ID)
public final class ChemOreXrayCommand {
    private static final TagKey<Block> XRAY_ORES = BlockTags.create(
            ResourceLocation.fromNamespaceAndPath(CreateChemworks.MOD_ID, "dev_xray_ores"));
    private static final int DEFAULT_RADIUS = 64;
    private static final int MAX_RESULTS_SHOWN = 32;

    private ChemOreXrayCommand() { }

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("chemxray")
                .requires(source -> source.hasPermission(2))
                .executes(context -> scan(context.getSource(), DEFAULT_RADIUS))
                .then(Commands.argument("radius", IntegerArgumentType.integer(8, 256))
                        .executes(context -> scan(context.getSource(),
                                IntegerArgumentType.getInteger(context, "radius")))));
    }

    private static int scan(net.minecraft.commands.CommandSourceStack source, int radius) {
        var level = source.getLevel();
        BlockPos center = BlockPos.containing(source.getPosition());
        int minChunkX = Math.floorDiv(center.getX() - radius, 16);
        int maxChunkX = Math.floorDiv(center.getX() + radius, 16);
        int minChunkZ = Math.floorDiv(center.getZ() - radius, 16);
        int maxChunkZ = Math.floorDiv(center.getZ() + radius, 16);
        long radiusSquared = (long) radius * radius;
        List<FoundOre> found = new ArrayList<>();
        int skippedChunks = 0;

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                var chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
                if (chunk == null) { skippedChunks++; continue; }
                var sections = chunk.getSections();
                for (int sectionIndex = 0; sectionIndex < sections.length; sectionIndex++) {
                    var section = sections[sectionIndex];
                    if (section.hasOnlyAir() || !section.maybeHas(state -> state.is(XRAY_ORES))) continue;
                    int sectionY = chunk.getMinBuildHeight() + sectionIndex * 16;
                    for (int localY = 0; localY < 16; localY++) for (int localZ = 0; localZ < 16; localZ++)
                        for (int localX = 0; localX < 16; localX++) {
                            var state = section.getBlockState(localX, localY, localZ);
                            if (!state.is(XRAY_ORES)) continue;
                            BlockPos pos = new BlockPos(chunkX * 16 + localX, sectionY + localY, chunkZ * 16 + localZ);
                            double distanceSquared = center.distSqr(pos);
                            if (distanceSquared <= radiusSquared) found.add(new FoundOre(pos, Math.sqrt(distanceSquared), state.getBlock().getName()));
                        }
                }
            }
        }

        found.sort(Comparator.comparingDouble(FoundOre::distance));
        int skipped = skippedChunks;
        source.sendSuccess(() -> Component.literal("Chemworks X-ray: found " + found.size() + " ore blocks within "
                + radius + " blocks" + (skipped > 0 ? " (skipped " + skipped + " unloaded chunks)" : ""))
                .withStyle(found.isEmpty() ? ChatFormatting.YELLOW : ChatFormatting.AQUA), false);
        found.stream().limit(MAX_RESULTS_SHOWN).forEach(ore -> {
            String coordinates = ore.pos().getX() + " " + ore.pos().getY() + " " + ore.pos().getZ();
            source.sendSuccess(() -> Component.literal(ore.name().getString() + " at " + coordinates
                    + " (" + Math.round(ore.distance()) + "m)")
                    .withStyle(style -> style.withColor(ChatFormatting.GOLD)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                                    "/tp @s " + coordinates))), false);
        });
        if (found.size() > MAX_RESULTS_SHOWN) {
            source.sendSuccess(() -> Component.literal("Showing the nearest " + MAX_RESULTS_SHOWN + " results."), false);
        }
        return found.size();
    }

    private record FoundOre(BlockPos pos, double distance, Component name) { }
}
