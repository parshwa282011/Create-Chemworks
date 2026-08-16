package com.parswha.createchemworks.chemistry;

import com.parswha.createchemworks.CreateChemworks;
import com.parswha.createchemworks.machinery.ChemicalStorageBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ConfigureChemicalTankPayload(BlockPos pos, double targetTemperatureK) implements CustomPacketPayload {
    public static final Type<ConfigureChemicalTankPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CreateChemworks.MOD_ID, "configure_chemical_tank"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ConfigureChemicalTankPayload> STREAM_CODEC = StreamCodec.of(
            (b, p) -> { b.writeBlockPos(p.pos); b.writeDouble(p.targetTemperatureK); },
            b -> new ConfigureChemicalTankPayload(b.readBlockPos(), b.readDouble()));
    public static void handle(ConfigureChemicalTankPayload payload, net.neoforged.neoforge.network.handling.IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().blockPosition().distSqr(payload.pos) > 64) return;
            if (context.player().level().getBlockEntity(payload.pos) instanceof ChemicalStorageBlockEntity tank) {
                try { tank.setTargetTemperatureK(payload.targetTemperatureK); }
                catch (IllegalArgumentException e) { context.player().sendSystemMessage(net.minecraft.network.chat.Component.literal(e.getMessage())); }
            }
        });
    }
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
