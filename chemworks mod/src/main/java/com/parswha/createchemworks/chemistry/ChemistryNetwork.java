package com.parswha.createchemworks.chemistry;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class ChemistryNetwork {
    private ChemistryNetwork() { }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar(Integer.toString(ReactionCalculator.GENERATOR_VERSION))
                .playToClient(ReactionSyncPayload.TYPE, ReactionSyncPayload.STREAM_CODEC,
                        (payload, context) -> ReactionCalculator.acceptServerBatch(
                                payload.reset(), payload.last(), payload.reactions()))
                .playToServer(SelectFlaskElementPayload.TYPE, SelectFlaskElementPayload.STREAM_CODEC,
                        SelectFlaskElementPayload::handle)
                .playToServer(ConfigureFlaskPayload.TYPE, ConfigureFlaskPayload.STREAM_CODEC,
                        ConfigureFlaskPayload::handle)
                .playToClient(OpenTesterPayload.TYPE,OpenTesterPayload.STREAM_CODEC,OpenTesterPayload::handle)
                .playToClient(OpenReactionBoardPayload.TYPE,OpenReactionBoardPayload.STREAM_CODEC,OpenReactionBoardPayload::handle)
                .playToServer(ConfigureTesterPayload.TYPE,ConfigureTesterPayload.STREAM_CODEC,ConfigureTesterPayload::handle)
                .playToServer(SaveReactionPayload.TYPE,SaveReactionPayload.STREAM_CODEC,SaveReactionPayload::handle);
    }
}
