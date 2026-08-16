package com.parswha.createchemworks.chemistry;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Persistent world catalogue for player-generated alloys. */
public final class WorldAlloyData extends SavedData {
    public static final String FILE_NAME = "create_chemworks_alloys";
    private final Map<ResourceLocation, Entry> alloys = new LinkedHashMap<>();

    public static Factory<WorldAlloyData> factory() {
        return new Factory<>(WorldAlloyData::new, WorldAlloyData::load);
    }

    public List<Entry> entries() { return List.copyOf(alloys.values()); }

    public void remember(ResourceLocation id, String name, AlloyProperties properties) {
        Entry replacement = new Entry(id, name, properties);
        if (!replacement.equals(alloys.put(id, replacement))) setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag entries = new ListTag();
        for (Entry entry : alloys.values()) {
            CompoundTag value = new CompoundTag();
            value.putString("Id", entry.id().toString());
            value.putString("Name", entry.name());
            AlloyProperties p = entry.properties();
            value.putDouble("Strength", p.strength()); value.putDouble("Hardness", p.hardness());
            value.putDouble("Toughness", p.toughness()); value.putDouble("AttackSpeed", p.attackSpeed());
            value.putDouble("Conductivity", p.conductivity()); value.putDouble("HeatResistance", p.heatResistance());
            value.putDouble("CorrosionResistance", p.corrosionResistance()); value.putDouble("Density", p.density());
            value.putDouble("Brittleness", p.brittleness()); value.putDouble("Toxicity", p.toxicity());
            value.putDouble("Instability", p.instability());
            ListTag traits = new ListTag(); p.positiveTraits().forEach(trait -> traits.add(StringTag.valueOf(trait)));
            value.put("PositiveTraits", traits); entries.add(value);
        }
        tag.put("Alloys", entries);
        return tag;
    }

    static WorldAlloyData load(CompoundTag tag, HolderLookup.Provider registries) {
        WorldAlloyData data = new WorldAlloyData();
        for (Tag raw : tag.getList("Alloys", Tag.TAG_COMPOUND)) {
            CompoundTag value = (CompoundTag) raw;
            ResourceLocation id = ResourceLocation.tryParse(value.getString("Id"));
            if (id == null) continue;
            List<String> traits = value.getList("PositiveTraits", Tag.TAG_STRING).stream()
                    .map(Tag::getAsString).toList();
            AlloyProperties p = new AlloyProperties(value.getDouble("Strength"), value.getDouble("Hardness"),
                    value.getDouble("Toughness"), value.getDouble("AttackSpeed"), value.getDouble("Conductivity"),
                    value.getDouble("HeatResistance"), value.getDouble("CorrosionResistance"), value.getDouble("Density"),
                    value.getDouble("Brittleness"), value.getDouble("Toxicity"), value.getDouble("Instability"), traits);
            data.alloys.put(id, new Entry(id, value.getString("Name"), p));
        }
        return data;
    }

    public record Entry(ResourceLocation id, String name, AlloyProperties properties) { }
}
