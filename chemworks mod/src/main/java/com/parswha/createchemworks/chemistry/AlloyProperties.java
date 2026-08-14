package com.parswha.createchemworks.chemistry;

import java.util.List;

public record AlloyProperties(
        double strength,
        double hardness,
        double toughness,
        double attackSpeed,
        double conductivity,
        double heatResistance,
        double corrosionResistance,
        double density,
        double brittleness,
        double toxicity,
        double instability,
        List<String> positiveTraits
) {
    public AlloyProperties {
        positiveTraits = List.copyOf(positiveTraits);
    }
}
