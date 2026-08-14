# mutil Proposal: Runtime Data Stores and Synchronization

## Summary

This proposal requests reusable runtime-data support in mutil for Tetra and other dependent mods.

Tetra currently builds its data-driven material system on mutil's merging data stores. Addons such as **Create: Chemworks** need to add definitions while a server is running, synchronize them to clients, preserve them across datapack reloads, and restore them from world data.

These generic capabilities fit mutil better than a chemistry- or Tetra-specific implementation. Tetra would provide its material API on top of them.

## Target versions

- Minecraft: 1.21.1
- NeoForge: 21.1.x
- mutil: 1.21.1-6.3.1
- Tetra integration target: 1.21.1-6.13.0

## Use case

Chemworks lets players discover metals and create alloys. Some definitions therefore do not exist during datapack loading. When an alloy is discovered:

- the server calculates its properties;
- Tetra registers a runtime material backed by mutil;
- connected clients receive the definition;
- newly joining clients receive the current catalogue;
- datapack reloads preserve the runtime layer;
- the provider restores world-specific definitions after a restart.

The same system could support runtime schematics, addon definitions, or other data-driven registries without each consumer implementing its own synchronization protocol.

## Runtime overlay for `MergingDataStore`

Add a supported runtime layer with these semantics:

- datapack definitions form the base layer;
- server runtime definitions form an overlay;
- providers can add, update, and remove namespaced entries;
- each entry records provider ownership;
- one provider cannot overwrite another provider's entry;
- runtime values use the store's normal merge and validation behavior;
- resource reloads rebuild the base without silently discarding the runtime overlay;
- the effective store view contains the merged base and runtime layers;
- changes produce a new monotonically increasing revision.

The API should avoid exposing mutable internal maps.

## Suggested API shape

The exact signature can follow mutil conventions:

```java
public interface RuntimeDataStore<K, V> {
    RegistrationResult register(ResourceLocation provider, K key, V value);
    RegistrationResult update(ResourceLocation provider, K key, V value);
    boolean remove(ResourceLocation provider, K key);
    Optional<V> get(K key);
    long revision();
}
```

Useful lifecycle callbacks or events include:

```java
RestoreRuntimeDataEvent
RuntimeDataChangedEvent
RuntimeDataSyncCompleteEvent
```

Registration and removal should be restricted to the logical server or an explicitly local client store.

## Provider ownership

Every runtime entry should record:

- a namespaced provider identifier;
- a namespaced entry identifier;
- an optional schema version;
- the registry revision at which it changed.

Ownership prevents accidental collisions and permits safe cleanup. A provider should be able to replace its own entries atomically during restoration without touching entries belonging to other mods.

Recommended operations include:

- replace all entries owned by one provider;
- remove all entries owned by one provider;
- begin and commit an atomic provider update;
- reject duplicate identifiers owned by another provider.

## Server-to-client synchronization

Provide a reusable distributor for runtime store data.

### Initial synchronization

When a player joins:

1. The server sends store identity, schema version, and current revision.
2. The server sends a validated snapshot in bounded batches.
3. The client builds a temporary view.
4. The client atomically publishes it after all batches arrive.
5. A completion callback allows dependent screens or caches to refresh.

### Delta synchronization

After initial synchronization, send revisioned deltas containing additions, updates, and removals. If a client misses a revision, it should request or receive a new snapshot rather than applying an unsafe partial history.

### Safety limits

The API should support configurable limits for:

- maximum entry count;
- maximum encoded entry size;
- maximum snapshot size;
- maximum batch size;
- decompression limits, if compression is used;
- decode and validation failures.

Clients must not be able to send registry mutations to the server. Unknown types or invalid data should fail safely and identify the store and entry involved.

## Serialization contracts

A runtime-capable store should accept:

- a disk/data codec;
- a network stream codec;
- entry validation;
- optional migration by schema version;
- existing merge behavior for layered definitions.

Runtime entries need not be persisted by mutil itself. The providing mod may own its saved data. mutil should make restoration ordering deterministic:

1. Datapack stores load.
2. Runtime providers receive a restoration callback.
3. Providers restore world-specific entries.
4. Stores validate and publish a completed revision.
5. Clients receive the resulting snapshots before dependent interfaces open.

## Reload behavior

Resource reloads must have documented behavior:

- rebuild the base datapack layer;
- retain or deliberately reconstruct the runtime layer;
- recalculate the effective merged view;
- avoid duplicate runtime registrations;
- increment the revision only when the effective view changes;
- notify dependent stores and clients after publication.

An atomic publication step is preferred so readers never observe half of a reloaded catalogue.

## Missing providers and recovery

If an item or saved object refers to a runtime entry whose provider is missing:

- preserve the identifier and remaining serialized data;
- return an explicit missing-entry result;
- avoid deleting or corrupting the containing item;
- allow the entry to recover if the provider returns;
- expose enough information for a consumer to show a useful message.

mutil only needs to provide the safe registry behavior. Tetra or the owning addon can decide how a missing material appears to players.

## Optional typed extension data

It would be useful for a store entry to contain addon-owned typed fields registered by namespaced identifiers. A generic facility could expose:

```java
public interface ExtensionDataType<T> {
    ResourceLocation id();
    Codec<T> codec();
    StreamCodec<RegistryFriendlyByteBuf, T> streamCodec();
}
```

Consumers such as Tetra could build property merging and gameplay semantics above this layer. mutil would only retain, validate, and synchronize the typed data.

Unknown optional extension fields should be preserved or skipped according to a documented compatibility policy.

## Threading and lifecycle

The API should document:

- which thread may mutate a store;
- when snapshots become visible;
- whether reads are safe during reload preparation;
- how consumers subscribe and unsubscribe;
- ordering between datapack reload, provider restoration, and player synchronization;
- behavior during integrated-server shutdown and world changes.

Mutations should normally be scheduled onto the logical server thread. Read APIs should expose immutable snapshots.

## Non-goals

- mutil does not need to know what a Tetra material is.
- mutil does not need to calculate chemistry or alloy properties.
- mutil does not need to persist another mod's complete domain model.
- Clients must not author server registry entries.
- Existing datapack-only stores should not be forced to become runtime stores.

## Acceptance criteria

1. A consumer can opt a merging data store into runtime overlays.
2. Providers can safely add, update, and remove owned entries.
3. Datapack and runtime entries produce one immutable effective view.
4. Resource reloads do not lose or duplicate runtime entries.
5. Connected clients receive bounded, revisioned deltas.
6. Newly joining clients receive an atomic current snapshot.
7. Missed or invalid revisions trigger safe resynchronization.
8. Clients cannot mutate the authoritative server store.
9. Entry codecs, validation, schema versions, and size limits are enforced.
10. Missing providers and missing entries fail safely.
11. Consumers can restore entries deterministically from saved world data.
12. Existing mutil data-store consumers continue to work unchanged.

## Questions for mutil's maintainers

1. Is a runtime overlay appropriate for `MergingDataStore`, or should it be a separate store type?
2. Can the current `DataDistributor` support revisioned snapshots and deltas?
3. Should mutil persist runtime entries, or should persistence always belong to providers?
4. What lifecycle point should providers use to restore world-specific entries?
5. Can immutable effective snapshots be published atomically without disrupting current reload behavior?
6. Would a generic typed-extension-data facility be useful, or should consumers encode extension maps themselves?

