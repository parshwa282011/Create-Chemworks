package com.parswha.createchemworks.chemistry;

import java.util.Map;

public record KnownCompound(
        String id,
        String name,
        String formula,
        String category,
        String standardPhase,
        Map<Integer, Integer> composition,
        double molarMass,
        double energyDensityMjKg,
        double ignitionTemperatureK,
        String hazards
) {
    public boolean isFuel() { return energyDensityMjKg > 0; }
}
