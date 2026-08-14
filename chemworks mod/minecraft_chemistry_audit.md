# Minecraft 1.21.1 Material Chemistry Audit

This report reviews the vanilla item and block registries using a strict chemical
definition of an **element**. Organic/biological materials, crafted assemblies,
mixtures, alloys, ores, minerals, salts, oxides, and storage blocks assembled from
other items are intentionally excluded from the pure-element list.

## Pure-element item candidates

| Minecraft item | Element | Symbol | Atomic number | Notes |
|---|---|---:|---:|---|
| `minecraft:diamond` | Carbon | C | 6 | Diamond is a crystalline allotrope of elemental carbon. |
| `minecraft:iron_ingot` | Iron | Fe | 26 | Best vanilla representation of refined elemental iron. Real iron products often contain small impurities. |
| `minecraft:iron_nugget` | Iron | Fe | 26 | A smaller form of the same refined material. |
| `minecraft:copper_ingot` | Copper | Cu | 29 | Best vanilla representation of refined elemental copper. |
| `minecraft:gold_ingot` | Gold | Au | 79 | Best vanilla representation of refined elemental gold. |
| `minecraft:gold_nugget` | Gold | Au | 79 | A smaller form of the same refined material. |

These six registry entries are the safest items to associate directly with normal
periodic-table elements.

## Elemental storage blocks excluded by the requested rule

These are chemically compatible with an element, but they are blocks crafted from
multiple units and were therefore left out of the main list:

| Block | Element |
|---|---|
| `minecraft:diamond_block` | Carbon |
| `minecraft:iron_block` | Iron |
| `minecraft:copper_block` | Copper |
| `minecraft:gold_block` | Gold |

Cut, chiseled, stair, slab, door, trapdoor, grate, bulb, chain, bars, rail, tool,
weapon, armor, pressure plate, and similar manufactured forms were also excluded.

## Element-related names that are not pure elements

| Minecraft material | Classification | Why it is not an element |
|---|---|---|
| `coal` | Carbon-rich organic sedimentary rock | Contains carbon plus many other substances. |
| `charcoal` | Organic-derived carbon-rich solid | Not chemically pure carbon. |
| `raw_iron` | Ore concentrate | Contains iron-bearing minerals and impurities. |
| `raw_copper` | Ore concentrate | Contains copper-bearing minerals and impurities. |
| `raw_gold` | Ore concentrate | Represents unrefined mineral material. |
| Iron, copper, and gold ores | Rocks/minerals | An ore contains compounds and surrounding rock, not isolated atoms of its named metal. |
| Exposed/weathered/oxidized copper | Oxides and patina | Copper has chemically reacted with oxygen and environmental compounds. |
| `netherite_scrap` | Fictional processed material | No corresponding real element. |
| `netherite_ingot` | Fictional alloy/composite | Crafted from netherite scrap and gold; explicitly not a pure element. |
| `emerald` | Mineral | Real emerald is beryl, approximately Be3Al2Si6O18, colored by trace elements. |
| `quartz` | Compound/mineral | Quartz is silicon dioxide, SiO2. |
| `amethyst_shard` | Compound/mineral | Amethyst is a variety of quartz, primarily SiO2. |
| `lapis_lazuli` | Mineral mixture | A rock dominated by lazurite and other minerals. |
| `flint` | Silica-rich material | Primarily microcrystalline silicon dioxide, not silicon. |
| `clay_ball` | Mineral mixture | Hydrated aluminosilicate minerals and impurities. |
| `brick` | Fired mineral mixture | Chemically altered clay, not an element. |
| `glass` and stained glass | Amorphous mixture | Primarily silica with modifiers and colorants. |
| `obsidian` | Volcanic glass | A mixture of silicate compounds. |
| `gunpowder` | Mixture | Traditionally contains an oxidizer, carbon, and sulfur-rich components. |

The same classification applies to blocks made from these materials.

## Fictional inorganic materials outside the normal periodic table

These are the strongest candidates for Chemworks' fictional chemistry. They do not
correspond to recognized chemical elements and should not be inserted into the real
periodic table without deliberately treating them as fictional elements, compounds,
minerals, or forms of energy.

| Material family | Representative registry entries | Recommended interpretation |
|---|---|---|
| Redstone | `redstone`, `redstone_ore`, `redstone_block` | Fictional conductive mineral or energetic compound. |
| Glowstone | `glowstone_dust`, `glowstone` | Fictional luminescent mineral/compound. |
| Prismarine | `prismarine_shard`, `prismarine_crystals`, prismarine blocks | Fictional marine mineral family. |
| Netherite | `netherite_scrap`, `netherite_ingot`, `netherite_block` | Fictional high-temperature alloy, not an element. |
| Ancient debris | `ancient_debris` | Fictional nether ore containing netherite precursors. |
| Echo material | `echo_shard` | Fictional resonance-bearing crystalline material. |
| End stone | `end_stone` | Fictional extraterrestrial rock or mineral mixture. |
| Crying obsidian | `crying_obsidian` | Magical/fictional variant of a real-world mixture. |
| Gilded blackstone | `gilded_blackstone` | Fictional composite rock containing gold-bearing material. |
| Soul materials | `soul_sand`, `soul_soil` | Supernatural soil mixtures, not elements. |
| Magma material | `magma_block`, `magma_cream` | Mixture or biological/magical material, not an element. |
| Sculk | All `sculk*` entries | Fictional biological or bio-mineral substance. |
| End crystal | `end_crystal` | Crafted magical device rather than a homogeneous material. |
| Experience | `experience_bottle` | Energy/game mechanic, not matter or an element. |

## Biological and organic families intentionally left unclassified

Following the requested rule, the audit does not treat the following as element
candidates: wood, leaves, saplings, flowers, crops, foods, dyes from biological
sources, leather, wool, feathers, eggs, meat, fish, coral, kelp, bamboo, fungi,
vines, honey, slime, mob drops, bones, shells, potions, and other organism-derived
items. Even when such an item contains a large amount of one element, it is still a
complex biological mixture.

## Recommended Chemworks mapping

- Put **C, Fe, Cu, and Au** on the normal periodic table and link the six pure-item
  candidates to them.
- Model ores and raw metals as recipes or mixtures that produce their corresponding
  pure element.
- Model quartz, amethyst, emerald, lapis, flint, clay, glass, and obsidian as
  compounds/minerals rather than new elements.
- Keep redstone, glowstone, prismarine, netherite, echo material, and other fictional
  substances in a separate fictional-material system unless the mod intentionally
  defines new H/M/A-block elements for them.
