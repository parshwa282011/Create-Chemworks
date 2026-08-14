package com.parswha.createchemworks.chemistry;

import com.parswha.createchemworks.client.Elements.ElectronBlock;
import com.parswha.createchemworks.client.Elements.ElementsData;

/** Deterministic gameplay kinetics model. Rates are estimates, not laboratory reference data. */
public record ReactionKinetics(boolean reversible, double forwardRate, double reverseRate, String unit) {
    private static final double GAS_CONSTANT = 8.314;
    public static ReactionKinetics estimate(ChemicalReaction reaction, double temperatureK, double pressureKpa,
                                            double leftConcentration, double rightConcentration, int catalysts) {
        double activationKj = Math.max(12.0, 48.0 + Math.abs(reaction.estimatedEnthalpyKj()) * 0.08 - catalysts * 9.0);
        long fElements = reaction.participatingElements().stream()
                .filter(number -> number > 0 && number <= ElementsData.all().size())
                .filter(number -> ElementsData.all().get(number - 1).block() == ElectronBlock.F).count();
        // F-orbital chemistry is intentionally dangerous: each participating F element
        // sharply raises collision frequency and lowers the gameplay activation barrier.
        activationKj = Math.max(4.0, activationKj - fElements * 11.0);
        double frequency = 2.5e6 * Math.pow(6.0, fElements) * Math.max(0.05, pressureKpa / 101.325);
        double forward = frequency * Math.exp(-activationKj * 1000.0 / (GAS_CONSTANT * Math.max(1, temperatureK)))
                * Math.max(0, leftConcentration) * Math.max(0, rightConcentration);
        boolean reversible = Math.abs(reaction.estimatedEnthalpyKj()) <= 180.0;
        double equilibrium = Math.exp(Math.max(-40, Math.min(40, -reaction.estimatedEnthalpyKj() * 1000.0
                / (GAS_CONSTANT * Math.max(1, temperatureK)))));
        double reverse = reversible ? forward / Math.max(1.0e-12, equilibrium) : 0;
        return new ReactionKinetics(reversible, round(forward), round(reverse), "mol/(L·s)");
    }
    private static double round(double n) { return Math.round(n * 1.0e9) / 1.0e9; }
}
