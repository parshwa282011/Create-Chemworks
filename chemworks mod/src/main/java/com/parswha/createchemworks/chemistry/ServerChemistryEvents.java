package com.parswha.createchemworks.chemistry;

import com.parswha.createchemworks.CreateChemworks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

@EventBusSubscriber(modid = CreateChemworks.MOD_ID)
public final class ServerChemistryEvents {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int SYNC_BATCH_SIZE = 500;
    private ServerChemistryEvents() { }

    @SubscribeEvent
    public static void serverStarted(ServerStartedEvent event) {
        WorldReactionData data = event.getServer().overworld().getDataStorage()
                .computeIfAbsent(WorldReactionData.factory(), WorldReactionData.FILE_NAME);
        LOGGER.info("Loaded {} on-demand chemical reactions from the world save",
                data.reactions().size());
        ReactionCalculator.replace(data.reactions());
    }

    @SubscribeEvent
    public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        List<ChemicalReaction> reactions = ReactionCalculator.reactions();
        if (reactions.isEmpty()) {
            PacketDistributor.sendToPlayer(player, new ReactionSyncPayload(true, true, List.of()));
            return;
        }
        for (int start = 0; start < reactions.size(); start += SYNC_BATCH_SIZE) {
            int end = Math.min(reactions.size(), start + SYNC_BATCH_SIZE);
            PacketDistributor.sendToPlayer(player, new ReactionSyncPayload(
                    start == 0, end == reactions.size(), reactions.subList(start, end)));
        }
    }
}
