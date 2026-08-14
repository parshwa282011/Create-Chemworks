# NeoForge 1.21.1 Modded Ores and Base Materials Audit

Checked July 20, 2026. “Ore” below means a mineable world-generation block. Ingots,
alloys, dusts, fluids, and compatibility entries are listed separately so they are
not mistaken for additional ores.

## Quick comparison

| Mod | Mineable material families added | Other notable base materials |
|---|---|---|
| Megalo's AIO 1.2.5.1 | 58 (50 real-element + 8 fictional) | 175 additional alloy ingots |
| Industrial Recrafted 1.0.3 | Tin, Uranium, Iridium | Bronze, refined iron, iridium alloy, mixed metal, advanced alloy, carbon plate |
| GeoSmelt 3.7.3 inspected | 11 families / 12 ore blocks | Xenoksmith, Deep Steel, Dark Steel, Eaglesteel and many processing forms |
| EvolutionTech 1.0.0 | Tin | Bronze; vanilla iron/copper/gold dusts and plates |
| Max's Tech Mod | Tin, Uranium | Meteorium, Super Alloy, Unobium are manufactured rather than mined |
| Create: Ironworks 4.0.3 | Tin | Bronze, Steel, coal dust, charcoal dust |
| Create: Metallurgy 1.0.3 | Wolframite | Tungsten, Steel, Obdurium, coke, graphite, slag |
| Create: Ore Refinery 0.1.1 | Flux | Crystallized, mantle, stabilized and sheet forms of Flux |
| Create: Miscellany 1.0.1 | Experience | Molten-material compatibility fluids; no new conventional metal ore |
| Ores and Metals 2.0.3 source | Tin, Mithril, Adamantite, Runite, Drakolith, Phasmatite | Bronze, Steel, Orikalkum, Necronium; Orichalcite and Necrite debris |
| Primitive Tech | Unverified | No uniquely matching 1.21.1 NeoForge project found |
| Create: Ores | Unverified | No uniquely matching current project matching the supplied description found |

## Megalo's AIO — exact ore families

### 50 real-element ore families

Aluminium, Antimony, Arsenic, Barium, Beryllium, Bismuth, Cadmium, Calcium,
Caesium, Chromium, Cobalt, Gallium, Germanium, Hafnium, Indium, Iridium, Lead,
Lithium, Magnesium, Manganese, Mercury (registry name `cinnabar_ore`), Molybdenum,
Nickel, Niobium, Osmium, Palladium, Phosphorus, Platinum, Potassium, Rhenium,
Rhodium, Rubidium, Ruthenium, Scandium, Selenium, Silicon, Silver, Sodium,
Strontium, Tantalum, Technetium, Tellurium, Thallium, Tin, Titanium, Tungsten,
Vanadium, Yttrium, Zinc, and Zirconium.

Each family has an ore, raw material, raw storage block, dust, nugget, and ingot.

### 8 fictional ore families

Adamantium, Amazonium, Dargonium, Kryptonium, Mithril, Orichalcum, Unobtainium,
and Vibranium.

### 175 additional ingot/alloy materials

AA-8000, Al Ga, Al-Li, Al-Ni-Co, Alu-Sca, Alumel, Aluminium Bronze, Anthracite
Iron, Argentium Sterling Silver, Arsenical Bronze, Arsenical Copper, Ashtadhatu,
Babbitt, Bell Metal, Beryllium Copper, Beta C, Billon, Birmabright, Bismanol,
Brass, Brightray, Britannia Silver, Britannium, Bronze, Bulat Steel, Calamine
Brass, Cast Iron, Cerrobend, Cerrosafe, Chinese Silver, Chrome Hydride, Chromoly,
Coin Silver, Colored Gold, Constantan, Copper Hydride, Copper-Nickel,
Copper-Tungsten, Corinthian Bronze, CrNi60WTi, Cromel, Crown Gold, Crucible Steel,
CuAg, Cunife, Cupronickel, Cymbal Alloy, Damascus Steel, Devarda's Alloy, Dore,
Ducol, Duralumin, Dutch Metal, Dymalloy, Electrum, Elektron, Elgiloy, Elinvar,
Fernico, Ferrochromium, Ferromagnesium, Ferromanganese, Ferromolybdenum,
Ferronickel, Ferrophosphorus, Ferrosilicon, Ferrotitanium, Ferrovanadium, Field's
Metal, Florentine Bronze, Galfenol, Galinstan, German Silver, Gilding Metal,
Glucydur, Goloid, Guanin, Gum Metal, Gun Metal, HSLA Steel, Hastelloy, Hausler
Alloy, Hepatizon, Hiduminium, High-Speed Steel, Hydronalium, Inconel, Inconel
686, Invar, Iron Hydride, Italma, KLi, Kanthal, Kovar, Lockalloy, MN40, MN70,
Magnalium, Magnox, Manganin, Maraging Steel, Megallium, Melchior, Molybdochalkos,
Monel Metal, Mu-Metal, Muntz Metal, Mushet Steel, NaK, Nambe, Ni-Ti-Al, Nichrome,
Nickel Hydride, Nickel Silver, Nickel-Carbon, Nicrosil, Nimonic, Nisil, Nitinol,
Nordic Gold, Ormolu, Permalloy, Pewter, Phosphor Bronze, Pig Iron, Pinchbeck,
Platinum Silver, Prince's Metal, Pseudo-Palladium, Queen's Metal, Reynolds 531,
Rhodite, Rose Gold, Rose Metal, Scandium Hydride, Shakudo, Shibuichi, Silicon
Bronze, Silicon Steel, Silumin, Silver Steel, Solder, Speculum Metal,
Spiegeleisen, Spring Steel, Staballoy, Stainless Steel, Steel, Stellite, Sterling
Silver, Supermalloy, T-Mg-Al-Zn, Talonite, Terne, Ti-6Al-4V, Tibetan Silver,
Titanium Gold, Titanium Hydride, Titanium Nitride, Tombac, Tool Steel, Tumbaga,
Type Metal, Ultimet, Vitallium, Weathering Steel, White Bronze, White Gold,
Wood's Metal, Wootz Steel, Wrought Iron, Y Alloy, Zamak, Zinc Amalgam, and
Zircaloy.

These 175 are not additional world-generated ores.

## Other mods in detail

### Industrial Recrafted

- Ores: Tin Ore, Deepslate Tin Ore, Uranium Ore, Deepslate Uranium Ore, Iridium
  Ore, and Deepslate Iridium Ore (three material families).
- Raw/refined forms: Raw Tin, Raw Uranium, Raw Iridium; Tin Ingot/Dust/Plate,
  Uranium Ingot, and Iridium Ingot.
- Manufactured materials: Bronze Dust/Ingot, Refined Iron Ingot, Iridium Alloy
  Ingot, Mixed Metal Ingot, Advanced Alloy Plate, and Carbon Plate.

### GeoSmelt

The inspected 3.7.3 JAR contains Nether Coal, Rose Quartz, Terranite, Deep Iron,
Netherite, Dark Iron, Viradium, Adamantium, Platinum, Starcinium, and Mithril ore
families. Platinum has normal and deepslate blocks, giving 12 ore blocks from 11
material families. The current project page advertises 12 ores, so the newer 4.1.9
release may have changed this roster.

Other crafting materials include Xeon Dust, Xenoksmith, Terranite Crystal, Raw
Netherite, Deep Steel, Dark Steel, and Eaglesteel, plus raw/crushed/dust/nugget/
sheet/crystal forms for several mined materials.

### EvolutionTech

- Ore: Tin Ore and Deepslate Tin Ore (one family).
- Forms: Raw Tin, Tin Ingot/Dust/Plate, and storage blocks.
- Alloy: Bronze Dust/Ingot/Plate, plus vanilla Iron/Copper/Gold dusts and plates.

### Max's Tech Mod

- Mineable ores: Tin and Uranium.
- Other metals: Meteorium, Super Alloy, and Unobium are described by the author as
  artificially crafted, so they should not be counted as ores.

### Create: Ironworks

- Ore: Tin Ore and Deepslate Tin Ore (one family).
- Forms: Raw Tin, Crushed Raw Tin, Tin Ingot/Nugget/Sheet and storage blocks.
- Alloys: Bronze and Steel in ingot, nugget, sheet, and block forms.
- Intermediates: Coal Dust and Charcoal Dust.

### Create: Metallurgy

- Ore: Wolframite Ore.
- Forms: Raw Wolframite, Crushed Raw Wolframite, Wolframite/Dirty Wolframite Dust,
  and Tungsten Ingot/Nugget/Sheet/Wire.
- Manufactured materials: Steel, Obdurium, Coke, Graphite, Slag, and Refractory
  Mortar.
- Its long molten-material list includes compatibility fluids such as Aluminum,
  Brass, Bronze, Constantan, Copper, Electrum, Gold, Invar, Iron, Lead, Lithium,
  Nickel, Osmium, Silver, Tin, and Zinc. Those entries are not new ores.

### Create: Ore Refinery

- Ore: Flux Ore.
- Forms: Crystallized Flux, Crystallized Flux Block, Mantle Flux, Stabilized Flux,
  Flux Sheet, and Flux Geyser.
- Its main purpose is producing ores belonging to Minecraft or other mods. Thorium
  support belongs to Create: New Age compatibility and is not a Thorium ore added
  by Ore Refinery itself.

### Create: Miscellany

- Ore: Experience Ore.
- Processing fluids: molten Electrum, Brass, Zinc, Copper, Silver, Steel, Bronze,
  Iron, Tin, Gold, and Netherite.
- Those molten entries are processing/compatibility content, not eleven new ores.

### Ores and Metals Mod

- World-generated families: Tin, Mithril, Adamantite, Runite, Nether Drakolith,
  and End Phasmatite. Tin, Mithril, Adamantite, and Runite also have deepslate
  variants.
- Debris-like mineables: Orichalcite Debris and Necrite Debris.
- Base/refined families: Tin, Bronze, Steel, Mithril, Adamantite, Runite,
  Drakolith, Phasmatite, Orikalkum, and Necronium.
- Bronze and Steel are materials/alloys, not generated ore families. Orikalkum and
  Necronium are upgraded materials made through their debris/scrap progression.

## Pack-design warning: heavy duplication

Installing all of these together creates competing versions of the same materials.
Tin appears in Megalo's AIO, Industrial Recrafted, EvolutionTech, Ironworks, Max's
Tech Mod, and Ores and Metals. Bronze and Steel are also registered by several
mods, while Mithril and Adamantium/Adamantite overlap across multiple mods.

NeoForge common tags can make recipes accept equivalent ingots, but tags do not
remove duplicate ores, textures, blocks, or storage items. A coherent pack should
choose one owner for each world-generated ore, disable the other worldgen entries,
and unify recipes and item outputs with KubeJS or a custom compatibility layer.

## Verification notes and sources

- [Megalo's AIO on Modrinth](https://modrinth.com/mod/megalos-aio-neoforge-edition)
- [Industrial Recrafted on CurseForge](https://www.curseforge.com/minecraft/mc-mods/industrial-recrafted)
- [GeoSmelt on CurseForge](https://www.curseforge.com/minecraft/mc-mods/geosmelt)
- [EvolutionTech on Modrinth](https://modrinth.com/mod/evolutiontech)
- [Max's Tech Mod author page](https://mcreator.net/modification/112581/maxs-tech-mod)
- [Create: Ironworks on Modrinth](https://modrinth.com/mod/create-ironworks)
- [Create: Metallurgy on CurseForge](https://www.curseforge.com/minecraft/mc-mods/create-metallurgy)
- [Create: Ore Refinery on Modrinth](https://modrinth.com/mod/create-ore-refinery)
- [Create: Miscellany on Modrinth](https://modrinth.com/mod/create-miscellany)
- [Ores and Metals source](https://github.com/Coliwogg/Ores-and-Metals)

Lists were derived from the named 1.21.1 NeoForge release JAR registries where a
download was available, and from the current project source/author description for
the remaining verified projects. “Primitive Tech” is too generic to resolve safely,
and the described “Create: Ores” project could not be matched to an authoritative
current 1.21.1 NeoForge listing.
