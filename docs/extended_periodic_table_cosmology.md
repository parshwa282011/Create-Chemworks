# Chemworks Periodic Table Rules

The current hardcoded table contains 558 elements: the 118 real elements followed
by 440 fictional elements. Alloys and compounds are excluded.

## Orbital occupancy

| Block | Occupied periods | Capacity per period | Total elements |
|---|---:|---:|---:|
| s | 1–13 | 2 | 26 |
| p | 2–13 | 6 | 72 |
| d | 4–13 | 10 | 100 |
| f | 6–13 | 14 | 112 |
| h | 8–13 | 18 | 108 |
| m | 10–13 | 22 | 88 |
| a | 12–13 | 26 | 52 |
| **Total** | | | **558** |

The real periodic table ends at Oganesson, element 118 in period 7. Fictional
elements begin at 119 in period 8 and end at 558 in period 13.

## Production rule

Every element in periods 10, 11, 12 and 13 is synthetic and may only be obtained
through a Particle Reaction performed by the future Particle Accelerator. This rule
covers 340 elements. These elements must never receive natural ore generation.

Periods 8 and 9 may use natural deposits or processing on the currently active
destinations: Mercury, Venus, Earth, the Moon and Mars.

## Current celestial scope

Only Mercury, Venus, Earth, the Moon and Mars are active planets/moons. Mercury,
Venus and Mars temporarily reuse Moon terrain. The outer planets and Sol 2 are
disabled until their world generation is ready.

## Source of truth

`ElementsData.java` contains an explicit hardcoded entry for every element. The CSV
and XLSX files are designer views generated from the same catalogue by
`tools/generate_periodic_catalogue.py`.
