package com.parswha.createchemworks.chemistry;

import java.util.List;
import java.util.Map;

/** Small, curated real-chemistry baseline. Values are gameplay-rounded. */
public final class KnownCompounds {
    private KnownCompounds() { }

    public static final List<KnownCompound> ALL = List.of(
            c("water", "Water", "H₂O", "Solvent", "Liquid", m(1,2,8,1), 18.015, 0, 0, "Generally safe"),
            c("hydrogen", "Hydrogen", "H₂", "Fuel", "Gas", m(1,2), 2.016, 120.0, 773, "Extremely flammable gas"),
            c("methane", "Methane", "CH₄", "Fuel", "Gas", m(6,1,1,4), 16.043, 50.0, 810, "Extremely flammable; asphyxiant"),
            c("ethane", "Ethane", "C₂H₆", "Fuel", "Gas", m(6,2,1,6), 30.070, 47.5, 788, "Extremely flammable gas"),
            c("propane", "Propane", "C₃H₈", "Fuel", "Gas", m(6,3,1,8), 44.097, 46.4, 743, "Extremely flammable gas"),
            c("butane", "Butane", "C₄H₁₀", "Fuel", "Gas", m(6,4,1,10), 58.124, 45.7, 678, "Extremely flammable gas"),
            c("pentane", "Pentane", "C₅H₁₂", "Fuel", "Liquid", m(6,5,1,12), 72.151, 45.4, 533, "Highly flammable liquid"),
            c("hexane", "Hexane", "C₆H₁₄", "Fuel/Solvent", "Liquid", m(6,6,1,14), 86.178, 45.0, 498, "Highly flammable; neurotoxic exposure"),
            c("octane", "Octane", "C₈H₁₈", "Fuel", "Liquid", m(6,8,1,18), 114.232, 44.4, 493, "Highly flammable liquid"),
            c("dodecane", "Dodecane", "C₁₂H₂₆", "Kerosene surrogate", "Liquid", m(6,12,1,26), 170.340, 44.0, 477, "Combustible liquid"),
            c("cetane", "Cetane", "C₁₆H₃₄", "Diesel surrogate", "Liquid", m(6,16,1,34), 226.448, 43.8, 475, "Combustible liquid"),
            c("ethene", "Ethene", "C₂H₄", "Fuel/Feedstock", "Gas", m(6,2,1,4), 28.054, 47.2, 763, "Extremely flammable gas"),
            c("ethyne", "Ethyne", "C₂H₂", "Fuel", "Gas", m(6,2,1,2), 26.038, 48.3, 578, "Extremely flammable; unstable under pressure"),
            c("methanol", "Methanol", "CH₄O", "Fuel/Solvent", "Liquid", m(6,1,1,4,8,1), 32.042, 22.7, 737, "Flammable; toxic"),
            c("ethanol", "Ethanol", "C₂H₆O", "Fuel/Solvent", "Liquid", m(6,2,1,6,8,1), 46.069, 26.8, 636, "Highly flammable"),
            c("propanol", "1-Propanol", "C₃H₈O", "Fuel/Solvent", "Liquid", m(6,3,1,8,8,1), 60.096, 30.6, 644, "Highly flammable"),
            c("butanol", "1-Butanol", "C₄H₁₀O", "Fuel/Solvent", "Liquid", m(6,4,1,10,8,1), 74.123, 33.1, 616, "Flammable; irritating"),
            c("benzene", "Benzene", "C₆H₆", "Fuel/Feedstock", "Liquid", m(6,6,1,6), 78.114, 40.2, 771, "Highly flammable; carcinogenic"),
            c("toluene", "Toluene", "C₇H₈", "Fuel/Solvent", "Liquid", m(6,7,1,8), 92.141, 40.6, 753, "Highly flammable; harmful vapour"),
            c("carbon_dioxide", "Carbon Dioxide", "CO₂", "Industrial Gas", "Gas", m(6,1,8,2), 44.009, 0, 0, "Asphyxiant at high concentration"),
            c("carbon_monoxide", "Carbon Monoxide", "CO", "Reducing Gas", "Gas", m(6,1,8,1), 28.010, 10.1, 878, "Flammable; acutely toxic"),
            c("ammonia", "Ammonia", "NH₃", "Fuel/Feedstock", "Gas", m(7,1,1,3), 17.031, 18.6, 924, "Toxic and corrosive gas"),
            c("hydrazine", "Hydrazine", "N₂H₄", "Rocket Fuel", "Liquid", m(7,2,1,4), 32.045, 19.4, 543, "Highly toxic; corrosive; reactive"),
            c("oxygen", "Oxygen", "O₂", "Oxidizer", "Gas", m(8,2), 31.998, 0, 0, "Strong oxidizer"),
            c("ozone", "Ozone", "O₃", "Oxidizer", "Gas", m(8,3), 47.997, 0, 0, "Strong oxidizer; toxic"),
            c("hydrogen_peroxide", "Hydrogen Peroxide", "H₂O₂", "Oxidizer", "Liquid", m(1,2,8,2), 34.015, 0, 0, "Oxidizing and corrosive when concentrated"),
            c("nitric_acid", "Nitric Acid", "HNO₃", "Acid/Oxidizer", "Liquid", m(1,1,7,1,8,3), 63.012, 0, 0, "Strong acid and oxidizer"),
            c("sulfuric_acid", "Sulfuric Acid", "H₂SO₄", "Acid", "Liquid", m(1,2,16,1,8,4), 98.079, 0, 0, "Strongly corrosive; dehydrating"),
            c("hydrochloric_acid", "Hydrogen Chloride", "HCl", "Acid", "Gas", m(1,1,17,1), 36.458, 0, 0, "Corrosive and toxic gas"),
            c("sodium_hydroxide", "Sodium Hydroxide", "NaOH", "Base", "Solid", m(11,1,8,1,1,1), 39.997, 0, 0, "Strongly corrosive base"),
            c("calcium_carbonate", "Calcium Carbonate", "CaCO₃", "Mineral", "Solid", m(20,1,6,1,8,3), 100.086, 0, 0, "Low hazard"),
            c("silicon_dioxide", "Silicon Dioxide", "SiO₂", "Mineral", "Solid", m(14,1,8,2), 60.084, 0, 0, "Respirable crystalline dust hazard"),
            c("aluminium_oxide", "Aluminium Oxide", "Al₂O₃", "Ceramic/Ore", "Solid", m(13,2,8,3), 101.961, 0, 0, "Dust irritant"),
            c("iron_oxide", "Iron(III) Oxide", "Fe₂O₃", "Ore/Oxide", "Solid", m(26,2,8,3), 159.687, 0, 0, "Low hazard; dust irritant"),
            c("ammonium_nitrate", "Ammonium Nitrate", "NH₄NO₃", "Fertilizer/Oxidizer", "Solid", m(7,2,1,4,8,3), 80.043, 0, 0, "Oxidizer; explosion hazard when contaminated"),
            c("urea", "Urea", "CH₄N₂O", "Fertilizer/Feedstock", "Solid", m(6,1,1,4,7,2,8,1), 60.056, 0, 0, "Low hazard"),
            c("acetone", "Acetone", "C₃H₆O", "Solvent", "Liquid", m(6,3,1,6,8,1), 58.080, 29.6, 738, "Highly flammable; irritating"),
            c("sodium_chloride", "Sodium Chloride", "NaCl", "Salt", "Solid", m(11,1,17,1), 58.443, 0, 0, "Low hazard")
    );

    public static KnownCompound find(String value) {
        String query = value.trim().replace("_", " ");
        return ALL.stream().filter(compound -> compound.id().replace("_", " ").equalsIgnoreCase(query)
                || compound.name().equalsIgnoreCase(query)
                || compound.formula().equalsIgnoreCase(value.trim())).findFirst().orElse(null);
    }

    private static KnownCompound c(String id, String name, String formula, String category,
                                   String phase, Map<Integer,Integer> composition, double mass,
                                   double energy, double ignition, String hazards) {
        return new KnownCompound(id, name, formula, category, phase, composition,
                mass, energy, ignition, hazards);
    }

    private static Map<Integer,Integer> m(int... pairs) {
        java.util.LinkedHashMap<Integer,Integer> result = new java.util.LinkedHashMap<>();
        for (int index = 0; index < pairs.length; index += 2) result.put(pairs[index], pairs[index + 1]);
        return Map.copyOf(result);
    }
}
