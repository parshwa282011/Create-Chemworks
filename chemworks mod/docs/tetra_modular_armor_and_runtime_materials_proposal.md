# Tetra Proposal: Modular Armor and Runtime Materials

## Summary

This proposal requests two extension areas for Tetra:

1. First-class modular armor.
2. A supported API through which addons can register materials and material properties at runtime.

The immediate integration target is **Create: Chemworks**, a Minecraft chemistry mod in which players discover metals and manufacture alloys. An alloy and its properties may not exist until a player creates it, so a fixed JSON definition for every possible alloy is impractical.

The integration should use documented APIs rather than reflection or mixins into Tetra internals. Existing datapack materials must continue to work unchanged.

## Target versions

- Minecraft: 1.21.1
- NeoForge: 21.1.x
- Tetra: 1.21.1-6.13.0
- mutil: 1.21.1-6.3.1

## Use case

Chemworks derives an alloy's characteristics from its elemental composition. Relevant values include:

- durability and structural integrity;
- mining efficiency and tool level;
- attack damage and attack speed;
- armor, armor toughness, and knockback resistance;
- electrical and thermal behavior;
- custom effects such as heat resistance, vibration damping, energy storage, or armor penetration.

The material catalogue can grow while a world is running. A newly discovered alloy should become usable in Tetra equipment without restarting the game or manually creating a datapack.

## Modular armor

### Requested equipment

Add modular archetypes for:

- helmet;
- chestplate;
- leggings;
- boots.

Armor should follow Tetra's established workflow: a base item, replaceable modules, improvements, repairs, material requirements, previews, and workbench/holosphere support.

### Suggested module slots

Shared slots could include:

- primary plate or shell;
- inner lining;
- bindings or joints;
- utility slot;
- optional trim or coating improvements.

Piece-specific modules could include:

- Helmet: visor, breathing unit, sensor mount.
- Chestplate: core plate, power or utility mount.
- Leggings: articulated joints, movement assist.
- Boots: soles, impact absorbers, traction modules.

The exact names and balance can follow Tetra's design language. The important requirement is that materials and effects can contribute independently to each module.

### Armor statistics

The system should support at least:

- armor points;
- armor toughness;
- knockback resistance;
- durability;
- integrity gain and cost;
- enchantability or magic capacity;
- movement-speed modifiers;
- arbitrary Minecraft attributes;
- Tetra effects and aspects;
- equipment-slot-aware modifiers.

Attributes must use stable modifier identifiers so recalculation, equipment changes, death, dimension transfer, and reconnecting cannot duplicate them.

### Rendering

Allow addons to supply:

- material tint colors;
- texture identifiers or layers;
- per-module models where supported;
- a tintable fallback for materials without custom assets.

A runtime material should not require a unique baked model merely to become usable.

### Compatibility

Modular armor should respect:

- vanilla armor and toughness calculations;
- enchantment applicability and exclusions;
- armor-bypassing damage tags;
- equipment-change events;
- death, respawn, and item serialization;
- server-authoritative attributes and effects;
- other mods that read standard armor attributes.

## Runtime material registration

### Required behavior

Expose a supported, server-authoritative API through which an addon can:

1. Register or update a material while a server is running.
2. Assign Tetra statistics, attributes, effects, aspects, tags, textures, and item matching rules.
3. Synchronize the definition to connected clients.
4. Preserve the material identity on modular items across restarts.
5. Restore dynamic registrations when a world loads.
6. Remove or invalidate a material safely when its provider is absent.

Runtime materials should coexist with datapack materials. Registration should not require a resource reload unless client assets genuinely require one.

### Stable identity

Every dynamic material should have a namespaced identifier, for example:

```text
create_chemworks:alloy/7f82c1...
```

Chemworks would derive this identifier from canonical alloy composition rather than a translated name. A definition should also carry a schema version, provider identifier, display component or translation key, and optional provider data.

### Suggested API shape

This signature is conceptual:

```java
public interface RuntimeMaterialRegistry {
    RegistrationResult register(ResourceLocation id, RuntimeMaterialDefinition definition);
    RegistrationResult update(ResourceLocation id, RuntimeMaterialDefinition definition);
    boolean unregister(ResourceLocation id);
    Optional<RuntimeMaterialDefinition> get(ResourceLocation id);
}
```

Registration should use a NeoForge event or another lifecycle-safe entry point rather than direct access to `MaterialStore`.

Possible events include:

```java
RegisterRuntimeMaterialsEvent
RegisterMaterialPropertyTypesEvent
GatherRuntimeMaterialDataEvent
```

The server must remain authoritative. Clients should receive validated snapshots or deltas and must not be allowed to mutate the server registry.

## Component-aware material matching

A runtime material should accept one or more of:

- an exact item;
- an item tag;
- an ingredient;
- a provider callback that validates a stack and reads its data components.

The callback form is essential for dynamic alloys. One `chemworks:alloy_ingot` item can contain a canonical composition component. Tetra would ask Chemworks to resolve that stack to a material rather than requiring a separate registered Minecraft item for every alloy.

The same lookup must work consistently for workbench inputs, repair materials, previews, recipes, and modular-item persistence.

## Extensible material properties

Tetra already supports core numeric fields, attributes, effects, aspects, and improvements. Addons additionally need a stable way to attach domain-specific properties without modifying Tetra classes.

Examples include thermal insulation, electrical conductivity, radiation shielding, chemical resistance, vibration damping, pressure tolerance, and energy storage.

### Requested property registry

Allow mods to register typed, namespaced material properties with codecs and merge behavior:

```java
public interface MaterialPropertyType<T> {
    ResourceLocation id();
    Codec<T> codec();
    StreamCodec<RegistryFriendlyByteBuf, T> streamCodec();
    T merge(List<WeightedValue<T>> values, MaterialMergeContext context);
}
```

Definitions could carry a property map such as:

```json
{
  "properties": {
    "create_chemworks:thermal_resistance": 0.85,
    "create_chemworks:vibration_damping": 12.0
  }
}
```

Tetra would retain, merge, serialize, synchronize, and expose the values. The owning addon would implement their gameplay behavior.

### Merge controls

Support additive, multiplicative, weighted-average, minimum, maximum, strongest-value, and provider-defined rules.

The merge context should identify the item archetype, module, material role, contribution weight, and equipment slot. This lets integrations calculate composite properties without guessing how a material was used.

### Effects and UI

An addon should be able to contribute:

- server-side action or equipment hooks;
- optional client rendering hooks;
- localized tooltips;
- stat bars or indicators;
- visibility rules for undiscovered properties.

Custom gameplay remains server-authoritative. If a client integration is absent, Tetra should display a generic readable property where practical rather than crash.

## Chemworks example workflow

When a player creates an alloy, Chemworks would:

1. Canonicalize its element percentages.
2. Derive a stable identifier from the composition.
3. Calculate Tetra stats and custom properties.
4. Register the runtime material on the logical server.
5. Store the composition on the alloy ingot as a data component.
6. Let Tetra resolve the component to the runtime material.
7. Synchronize the definition to clients.
8. Re-register discovered alloys from saved data on world load.

Chemworks contains 36 fictional `t`-orbital metals, each with one positive trait. Alloys merge those traits, including a complete endgame alloy containing all 36. Tetra should expose the integration points but does not need to understand the chemistry model.

## Non-goals

- Tetra does not need to calculate chemistry.
- This proposal does not replace existing datapack materials.
- Clients must not be able to alter server materials.
- Runtime materials must not each require a unique texture or model.
- Tetra does not need to persist Chemworks' complete domain model.

## Acceptance criteria

1. An addon can add a material using documented APIs only.
2. An addon can resolve a material from an item's data component.
3. The material becomes usable at a Tetra workbench without a restart.
4. Stats, attributes, effects, aspects, and custom properties work correctly.
5. Connected and newly joining clients receive the server definition.
6. Modular items using the material survive saving and restarting.
7. Datapack reloads do not lose or duplicate runtime definitions.
8. A missing provider produces a safe missing-material state.
9. Helmet, chestplate, leggings, and boots accept datapack and runtime materials.
10. Armor attributes do not duplicate after repeated equipment changes or reconnects.
11. Existing Tetra material packs work without modification.
12. Addons do not require mixins into Tetra or mutil internals.

## Questions for Tetra's maintainers

1. Is runtime mutation compatible with Tetra's current material synchronization model?
2. Would transient definitions or generated server datapack entries fit Tetra better?
3. Can component-aware lookup extend the current `OutcomeMaterial` design?
4. Is the dynamic modular-item archetype system suitable for armor?
5. Which hooks would be acceptable for addon-owned effects and stat displays?
6. Which runtime-store responsibilities should be delegated to mutil?

