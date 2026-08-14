package com.parswha.createchemworks.world;

import com.parswha.createchemworks.CreateChemworks;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;

import java.util.HashMap;
import java.util.Map;

/** Assigns the two chaos cave biomes by depth while preserving every vanilla biome above y=-64. */
@EventBusSubscriber(modid = CreateChemworks.MOD_ID)
public final class ChaosBiomeEvents {
    private ChaosBiomeEvents() { }

    @SubscribeEvent
    public static void chunkLoad(ChunkEvent.Load event) {
        if (!event.isNewChunk() || !(event.getLevel() instanceof ServerLevel level)
                || level.dimension() != Level.OVERWORLD) return;
        ChunkAccess chunk = event.getChunk();
        var registry = level.registryAccess().registryOrThrow(Registries.BIOME);
        Holder<Biome> depths = registry.getHolderOrThrow(key("chaos_depths"));
        Holder<Biome> abyss = registry.getHolderOrThrow(key("chaos_abyss"));
        int minQuartY = Math.floorDiv(chunk.getMinBuildHeight(), 4);
        int maxQuartY = Math.floorDiv(chunk.getMinBuildHeight() + chunk.getHeight() - 1, 4);
        int startX = chunk.getPos().getMinBlockX() >> 2, startZ = chunk.getPos().getMinBlockZ() >> 2;
        Map<Long, Holder<Biome>> original = new HashMap<>();
        for (int x = startX; x < startX + 4; x++) for (int z = startZ; z < startZ + 4; z++)
            for (int y = minQuartY; y <= maxQuartY; y++) original.put(pack(x, y, z), chunk.getNoiseBiome(x, y, z));
        chunk.fillBiomesFromNoise((x, y, z, sampler) -> y * 4 < -160 ? abyss
                        : y * 4 < -64 ? depths : original.getOrDefault(pack(x, y, z), depths),
                level.getChunkSource().randomState().sampler());
        chunk.setUnsaved(true);
    }

    private static ResourceKey<Biome> key(String path) {
        return ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(CreateChemworks.MOD_ID, path));
    }
    private static long pack(int x, int y, int z) {
        return ((long) x & 0x3fffffL) << 42 | ((long) z & 0x3fffffL) << 20 | ((long) y & 0xfffffL);
    }
}
