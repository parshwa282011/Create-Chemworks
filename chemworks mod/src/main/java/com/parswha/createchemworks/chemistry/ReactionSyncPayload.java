package com.parswha.createchemworks.chemistry;

import com.parswha.createchemworks.CreateChemworks;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record ReactionSyncPayload(boolean reset, boolean last, List<ChemicalReaction> reactions)
        implements CustomPacketPayload {
    public static final Type<ReactionSyncPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(CreateChemworks.MOD_ID, "reaction_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ReactionSyncPayload> STREAM_CODEC =
            StreamCodec.of(ReactionSyncPayload::encode, ReactionSyncPayload::decode);

    private static void encode(RegistryFriendlyByteBuf buffer, ReactionSyncPayload payload) {
        buffer.writeBoolean(payload.reset);
        buffer.writeBoolean(payload.last);
        buffer.writeVarInt(payload.reactions.size());
        for (ChemicalReaction reaction : payload.reactions) {
            buffer.writeResourceLocation(reaction.id());
            buffer.writeUtf(reaction.equation());
            buffer.writeVarInt(reaction.participatingElements().size());
            reaction.participatingElements().forEach(buffer::writeVarInt);
            buffer.writeDouble(reaction.minimumTemperatureK());
            buffer.writeDouble(reaction.pressureKpa());
            buffer.writeDouble(reaction.estimatedEnthalpyKj());
            buffer.writeUtf(reaction.catalyst());
            buffer.writeUtf(reaction.confidence());
        }
    }

    private static ReactionSyncPayload decode(RegistryFriendlyByteBuf buffer) {
        boolean reset = buffer.readBoolean();
        boolean last = buffer.readBoolean();
        int count = buffer.readVarInt();
        List<ChemicalReaction> reactions = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            ResourceLocation id = buffer.readResourceLocation();
            String equation = buffer.readUtf();
            int participantCount = buffer.readVarInt();
            List<Integer> participants = new ArrayList<>(participantCount);
            for (int participant = 0; participant < participantCount; participant++) {
                participants.add(buffer.readVarInt());
            }
            reactions.add(new ChemicalReaction(id, equation, participants,
                    buffer.readDouble(), buffer.readDouble(), buffer.readDouble(),
                    buffer.readUtf(), buffer.readUtf()));
        }
        return new ReactionSyncPayload(reset, last, reactions);
    }

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
