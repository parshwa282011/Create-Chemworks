package com.parswha.createchemworks.chemistry;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.LinkedHashMap;
import java.util.Map;

public final class WorldReactionData extends SavedData {
    public static final String FILE_NAME = "create_chemworks_reactions";
    private int generatorVersion;
    private List<ChemicalReaction> reactions;
    private Map<String, CalculatedCompound> compounds = Map.of();

    public WorldReactionData() {
        this(0, List.of());
    }

    private WorldReactionData(int generatorVersion, List<ChemicalReaction> reactions) {
        this.generatorVersion = generatorVersion;
        this.reactions = List.copyOf(reactions);
    }

    public static Factory<WorldReactionData> factory() {
        return new Factory<>(WorldReactionData::new, WorldReactionData::load);
    }

    public boolean needsGeneration(boolean developmentMode) {
        return false;
    }

    public void regenerate() {
        reactions = List.of();
        generatorVersion = ReactionCalculator.GENERATOR_VERSION;
        setDirty();
    }

    public List<ChemicalReaction> reactions() { return reactions; }

    public Optional<ChemicalReaction> find(String equation) {
        return reactions.stream().filter(value -> value.equation().equals(equation)).findFirst();
    }

    public ChemicalReaction remember(ChemicalReaction reaction) {
        Optional<ChemicalReaction> existing = find(reaction.equation());
        if (existing.isPresent()) return existing.get();
        List<ChemicalReaction> updated = new ArrayList<>(reactions);
        updated.add(reaction);
        reactions = List.copyOf(updated);
        generatorVersion = ReactionCalculator.GENERATOR_VERSION;
        setDirty();
        ReactionCalculator.replace(reactions);
        return reaction;
    }

    public ChemicalReaction remember(LiveReactionCalculator.Result result) {
        Map<String, CalculatedCompound> updated = new LinkedHashMap<>(compounds);
        updated.putIfAbsent(result.product().formula(), result.product());
        compounds = Map.copyOf(updated);
        setDirty();
        return remember(result.reaction());
    }

    public Map<String, CalculatedCompound> compounds() { return compounds; }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("GeneratorVersion", generatorVersion);
        ListTag entries = new ListTag();
        for (ChemicalReaction reaction : reactions) {
            CompoundTag value = new CompoundTag();
            value.putString("Id", reaction.id().toString());
            value.putString("Equation", reaction.equation());
            value.putIntArray("Elements", reaction.participatingElements());
            value.putDouble("TemperatureK", reaction.minimumTemperatureK());
            value.putDouble("PressureKpa", reaction.pressureKpa());
            value.putDouble("EnthalpyKj", reaction.estimatedEnthalpyKj());
            value.putString("Catalyst", reaction.catalyst());
            value.putString("Confidence", reaction.confidence());
            entries.add(value);
        }
        tag.put("Reactions", entries);
        ListTag compoundEntries = new ListTag();
        for (CalculatedCompound compound : compounds.values()) {
            CompoundTag value = new CompoundTag();
            value.putString("Formula", compound.formula());
            int[] composition = new int[compound.composition().size() * 2];
            int cursor = 0;
            for (var atom : compound.composition().entrySet()) {
                composition[cursor++] = atom.getKey();
                composition[cursor++] = atom.getValue();
            }
            value.putIntArray("Composition", composition);
            value.putDouble("MolarMass", compound.molarMass());
            value.putString("BondType", compound.bondType());
            value.putString("Phase", compound.phase());
            value.putDouble("MeltingPointK", compound.meltingPointK());
            value.putDouble("BoilingPointK", compound.boilingPointK());
            value.putString("Confidence", compound.confidence());
            compoundEntries.add(value);
        }
        tag.put("Compounds", compoundEntries);
        return tag;
    }

    private static WorldReactionData load(CompoundTag tag, HolderLookup.Provider registries) {
        List<ChemicalReaction> reactions = new ArrayList<>();
        ListTag entries = tag.getList("Reactions", Tag.TAG_COMPOUND);
        for (Tag raw : entries) {
            CompoundTag value = (CompoundTag) raw;
            reactions.add(new ChemicalReaction(
                    ResourceLocation.parse(value.getString("Id")), value.getString("Equation"),
                    Arrays.stream(value.getIntArray("Elements")).boxed().toList(),
                    value.getDouble("TemperatureK"), value.getDouble("PressureKpa"),
                    value.getDouble("EnthalpyKj"), value.getString("Catalyst"),
                    value.getString("Confidence")));
        }
        WorldReactionData data = new WorldReactionData(tag.getInt("GeneratorVersion"), reactions);
        Map<String, CalculatedCompound> compounds = new LinkedHashMap<>();
        for (Tag raw : tag.getList("Compounds", Tag.TAG_COMPOUND)) {
            CompoundTag value = (CompoundTag) raw;
            int[] rawComposition = value.getIntArray("Composition");
            Map<Integer, Integer> composition = new LinkedHashMap<>();
            for (int index = 0; index + 1 < rawComposition.length; index += 2)
                composition.put(rawComposition[index], rawComposition[index + 1]);
            CalculatedCompound compound = new CalculatedCompound(value.getString("Formula"),
                    Map.copyOf(composition), value.getDouble("MolarMass"), value.getString("BondType"),
                    value.getString("Phase"), value.getDouble("MeltingPointK"),
                    value.getDouble("BoilingPointK"), value.getString("Confidence"));
            compounds.put(compound.formula(), compound);
        }
        data.compounds = Map.copyOf(compounds);
        return data;
    }
}
