#!/usr/bin/env python3
"""Generate temporary Moon-like dimensions and link them to Cosmonautics planets."""

from __future__ import annotations

import json
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
DATA = ROOT / "src/main/resources/data/create_chemworks"
PLANET_DIR = DATA / "universe_planets"
DIMENSION_DIR = DATA / "dimension"
PHYSICS_DIR = DATA / "dimension_physics"

# body: (surface gravity, day controller, breathable lower atmosphere)
PLANETS = {
    "mercury": (3.70, "sol", False),
    "venus": (8.87, "sol", False),
    "mars": (3.71, "sol", False),
}

DISABLED_BODIES = ("jupiter", "saturn", "uranus", "neptune", "pluto", "sol_2", "aurelia", "nyx")


def write_json(path: Path, value: object) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, indent=2) + "\n", encoding="utf-8")


def dimension_definition() -> dict[str, object]:
    return {
        "type": "rocketnautics:moon",
        "generator": {
            "type": "minecraft:noise",
            "biome_source": {
                "type": "minecraft:fixed",
                "biome": "rocketnautics:lunar_highlands",
            },
            "settings": "rocketnautics:moon",
        },
    }


def dimension_data(body: str, controller: str, breathable: bool) -> dict[str, object]:
    atmosphere = {"21000": ["low_density", "drowning"]}
    if breathable:
        atmosphere = {"5000": [], "21000": ["low_density", "drowning"]}
    return {
        "allowed_transfer": "all",
        "apply_gravity_correction_to_entities_in_dimension": True,
        "atmosphere_composition": atmosphere,
        "dimension_day_time_controller_name": controller,
        "dimension_transfer_height": 20000,
        "entity_drag_multiplier": [
            {"altitude": 4000.0, "slope": 0.0, "value": 1.0},
            {"altitude": 7000.0, "slope": -0.0006, "value": 0.0},
        ],
        "linked_dimension": f"create_chemworks:{body}",
        "render_universe_in_dimension": True,
    }


def main() -> None:
    for body, (gravity, controller, breathable) in PLANETS.items():
        write_json(DIMENSION_DIR / f"{body}.json", dimension_definition())
        write_json(PHYSICS_DIR / f"{body}.json", {
            "dimension": f"create_chemworks:{body}",
            "priority": 1000,
            "universal_drag": 0,
            "base_gravity": [0, -gravity, 0],
            "base_pressure": 1 if breathable else 0,
            "magnetic_north": [0, 0, 0],
        })
        path = PLANET_DIR / f"{body}.json"
        planet = json.loads(path.read_text(encoding="utf-8")) if path.exists() else {
            "parent": "sol", "name": body,
        }
        planet["dimension_data"] = dimension_data(body, controller, breathable)
        planet["priority"] = 1000
        write_json(path, planet)
    for body in DISABLED_BODIES:
        path = PLANET_DIR / f"{body}.json"
        planet = json.loads(path.read_text(encoding="utf-8"))
        planet.pop("dimension_data", None)
        planet["disabled"] = True
        planet["priority"] = 1000
        write_json(path, planet)
    print(f"Generated and linked {len(PLANETS)} temporary planet dimensions")


if __name__ == "__main__":
    main()
