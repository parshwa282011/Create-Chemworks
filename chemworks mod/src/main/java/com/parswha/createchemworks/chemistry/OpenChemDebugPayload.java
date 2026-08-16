package com.parswha.createchemworks.chemistry;

import com.parswha.createchemworks.CreateChemworks;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record OpenChemDebugPayload(List<String> lines) implements CustomPacketPayload {
    public static final Type<OpenChemDebugPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CreateChemworks.MOD_ID, "open_chem_debug"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenChemDebugPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeVarInt(payload.lines.size());
                payload.lines.forEach(line -> buffer.writeUtf(line, 1024));
            }, buffer -> {
                int count = Math.min(buffer.readVarInt(), 512);
                List<String> lines = new ArrayList<>(count);
                for (int i = 0; i < count; i++) lines.add(buffer.readUtf(1024));
                return new OpenChemDebugPayload(List.copyOf(lines));
            });

    public static void handle(OpenChemDebugPayload payload, net.neoforged.neoforge.network.handling.IPayloadContext context) {
        context.enqueueWork(() -> com.parswha.createchemworks.client.ClientEvents.openChemDebugScreen(payload));
    }

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
