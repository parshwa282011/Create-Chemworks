package com.parswha.createchemworks.chemistry;

import com.parswha.createchemworks.client.Elements.Element;

import java.util.List;

/** Compact, deterministic chemistry properties derived from an element's table position. */
public record ElementProfile(
        double atomicMass,
        String electronConfiguration,
        int valenceElectrons,
        List<Integer> oxidationStates,
        double electronegativity,
        double atomicRadiusPm,
        double density,
        double meltingPointK,
        double boilingPointK,
        String standardPhase,
        String stability,
        String provenance
) {
    public static ElementProfile calculate(Element element) {
        int valence = ChemistryRules.valenceElectrons(element);
        double mass = element.number() == 1 ? 1.008 : round(element.number() * (2.02 + element.period() * 0.015));
        double electronegativity = round(Math.max(0.7, Math.min(4.0,
                0.75 + element.column() * 0.14 - element.period() * 0.025)));
        double radius = round(Math.max(28, 42 + element.period() * 24 - element.column() * 1.8));
        double density = round(Math.max(0.0001,
                mass / Math.max(5.0, radius * radius * radius / 155000.0)));
        double melting = round(55 + 48.0 * element.period() + 31.0 * Math.max(1, valence));
        double boiling = round(melting + 180 + 42.0 * Math.max(1, valence));
        String phase = boiling < 298.15 ? "Gas" : melting > 298.15 ? "Solid" : "Liquid";
        String stability = element.number() <= 82 ? "Predicted stable/common isotopes"
                : element.acceleratorOnly() ? "Synthetic; accelerator-only" : "Radioactive or unstable";
        String provenance = element.number() <= 118
                ? "Calculated placeholder; calibrate against measured real-world data"
                : "Predicted from extended periodic-table position";
        return new ElementProfile(mass, electronConfiguration(element), valence,
                ChemistryRules.commonOxidationStates(element), electronegativity, radius,
                density, melting, boiling, phase, stability, provenance);
    }

    private static String electronConfiguration(Element element) {
        int shell = element.period();
        int occupancy = Math.min(element.block().capacity(), Math.max(1, element.column() + 1));
        return "[previous shell] " + shell + element.block().label() + occupancy;
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
