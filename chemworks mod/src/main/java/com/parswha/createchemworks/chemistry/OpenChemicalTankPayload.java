package com.parswha.createchemworks.chemistry;

import com.parswha.createchemworks.CreateChemworks;
import com.parswha.createchemworks.machinery.ChemicalStorageBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenChemicalTankPayload(BlockPos pos, String chemical, double moles, double volumeLiters,
        double temperatureK, double pressureKpa, double targetTemperatureK, int energy, int maxEnergy)
        implements CustomPacketPayload {
    public static final Type<OpenChemicalTankPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CreateChemworks.MOD_ID, "open_chemical_tank"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenChemicalTankPayload> STREAM_CODEC = StreamCodec.of(
            (b, p) -> { b.writeBlockPos(p.pos); b.writeUtf(p.chemical); b.writeDouble(p.moles); b.writeDouble(p.volumeLiters); b.writeDouble(p.temperatureK); b.writeDouble(p.pressureKpa); b.writeDouble(p.targetTemperatureK); b.writeVarInt(p.energy); b.writeVarInt(p.maxEnergy); },
            b -> new OpenChemicalTankPayload(b.readBlockPos(), b.readUtf(512), b.readDouble(), b.readDouble(), b.readDouble(), b.readDouble(), b.readDouble(), b.readVarInt(), b.readVarInt()));
    public static OpenChemicalTankPayload from(BlockPos pos, ChemicalStorageBlockEntity tank) {
        var volume = tank.chemicalVolume();
        return new OpenChemicalTankPayload(pos, volume.chemical(), volume.moles(), volume.volumeLiters(),
                volume.temperatureK(), volume.pressureKpa(), tank.targetTemperatureK(),
                tank.energyStorage().getEnergyStored(), tank.energyStorage().getMaxEnergyStored());
    }
    public static void handle(OpenChemicalTankPayload payload, net.neoforged.neoforge.network.handling.IPayloadContext context) {
        context.enqueueWork(() -> { try { Class<?> c = Class.forName("com.parswha.createchemworks.client.ClientEvents"); c.getMethod("openChemicalTankScreen", OpenChemicalTankPayload.class).invoke(null, payload); } catch (ReflectiveOperationException e) { throw new IllegalStateException(e); } });
    }
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
