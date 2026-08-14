package com.parswha.createchemworks.chemistry;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/** Serializable result used by tester lists, reusable schematics, boards, and machinery. */
public record ReactionResultSnapshot(
        String id, String equation, List<Integer> elements, double minimumTemperatureK,
        double pressureKpa, double enthalpyKj, String catalyst, String confidence,
        boolean viable, boolean reversible, double forwardRate, double reverseRate, String unit) {
    public ReactionResultSnapshot { elements = List.copyOf(elements); }

    public static ReactionResultSnapshot from(ReactionCandidateGenerator.Candidate candidate) {
        ChemicalReaction reaction = candidate.result().reaction();
        ReactionKinetics rates = candidate.kinetics();
        return new ReactionResultSnapshot(reaction.id().toString(), reaction.equation(),
                reaction.participatingElements(), reaction.minimumTemperatureK(), reaction.pressureKpa(),
                reaction.estimatedEnthalpyKj(), reaction.catalyst(), reaction.confidence(),
                candidate.result().conditionsMet(), rates.reversible(), rates.forwardRate(),
                rates.reverseRate(), rates.unit());
    }

    public ChemicalReaction reaction() {
        return new ChemicalReaction(ResourceLocation.parse(id), equation, elements, minimumTemperatureK,
                pressureKpa, enthalpyKj, catalyst, confidence);
    }

    public void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeUtf(id, 256); buffer.writeUtf(equation, 2048);
        buffer.writeVarInt(elements.size()); elements.forEach(buffer::writeVarInt);
        buffer.writeDouble(minimumTemperatureK); buffer.writeDouble(pressureKpa); buffer.writeDouble(enthalpyKj);
        buffer.writeUtf(catalyst, 256); buffer.writeUtf(confidence, 512);
        buffer.writeBoolean(viable); buffer.writeBoolean(reversible);
        buffer.writeDouble(forwardRate); buffer.writeDouble(reverseRate); buffer.writeUtf(unit, 64);
    }

    public static ReactionResultSnapshot read(RegistryFriendlyByteBuf buffer) {
        String id = buffer.readUtf(256), equation = buffer.readUtf(2048);
        int count = buffer.readVarInt();
        if (count < 0 || count > 218) throw new IllegalArgumentException("Invalid element count");
        var elements = new java.util.ArrayList<Integer>(count);
        for (int i = 0; i < count; i++) elements.add(buffer.readVarInt());
        return new ReactionResultSnapshot(id, equation, elements, buffer.readDouble(), buffer.readDouble(),
                buffer.readDouble(), buffer.readUtf(256), buffer.readUtf(512), buffer.readBoolean(),
                buffer.readBoolean(), buffer.readDouble(), buffer.readDouble(), buffer.readUtf(64));
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Id", id); tag.putString("Equation", equation);
        tag.putIntArray("Elements", elements); tag.putDouble("MinimumTemperatureK", minimumTemperatureK);
        tag.putDouble("PressureKpa", pressureKpa); tag.putDouble("EnthalpyKj", enthalpyKj);
        tag.putString("Catalyst", catalyst); tag.putString("Confidence", confidence);
        tag.putBoolean("Viable", viable); tag.putBoolean("Reversible", reversible);
        tag.putDouble("ForwardRate", forwardRate); tag.putDouble("ReverseRate", reverseRate); tag.putString("Unit", unit);
        return tag;
    }

    public static ReactionResultSnapshot fromTag(CompoundTag tag) {
        return new ReactionResultSnapshot(tag.getString("Id"), tag.getString("Equation"),
                java.util.Arrays.stream(tag.getIntArray("Elements")).boxed().toList(),
                tag.getDouble("MinimumTemperatureK"), tag.getDouble("PressureKpa"), tag.getDouble("EnthalpyKj"),
                tag.getString("Catalyst"), tag.getString("Confidence"), tag.getBoolean("Viable"),
                tag.getBoolean("Reversible"), tag.getDouble("ForwardRate"), tag.getDouble("ReverseRate"), tag.getString("Unit"));
    }
}
