package com.parswha.createchemworks.chemistry;

import com.parswha.createchemworks.client.Elements.Element;

import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;
import com.parswha.createchemworks.client.Elements.ElectronBlock;

/** Rule-of-mixtures alloy model with size, valence and electronegativity interactions. */
public final class AlloyCalculator {
    private AlloyCalculator() { }

    public static AlloyProperties calculate(List<Constituent> constituents) {
        if (constituents.isEmpty()) throw new IllegalArgumentException("At least one constituent is required");
        double total = constituents.stream().mapToDouble(Constituent::percent).sum();
        if (Math.abs(total - 100.0) > 0.05) {
            throw new IllegalArgumentException("Concentrations must total 100%; got " + round(total) + "%");
        }
        double metalPercent = constituents.stream()
                .filter(value -> ChemistryRules.isMetal(value.element()))
                .mapToDouble(Constituent::percent).sum();
        if (metalPercent < 50) throw new IllegalArgumentException("An alloy must contain at least 50% metal");

        double strength = 0, hardness = 0, toughness = 0, conductivity = 0;
        double heat = 0, corrosion = 0, density = 0, brittleness = 0, toxicity = 0, instability = 0;
        Set<String> traits = new LinkedHashSet<>();
        for (Constituent constituent : constituents) {
            Element element = constituent.element();
            ElementProfile profile = ElementProfile.calculate(element);
            double fraction = constituent.percent() / 100.0;
            double depth = 1.0 + (element.period() - 1) * 0.13;
            boolean tMetal = element.block() == ElectronBlock.T;
            double valence = Math.max(1, profile.valenceElectrons());
            double toughnessContribution = depth * (65 - valence * 3 + profile.atomicRadiusPm() * 0.08);
            double tPower = tMetal ? 4.0 : 1.0;
            strength += fraction * depth * tPower * (28 + valence * 10 + profile.meltingPointK() * 0.035);
            hardness += fraction * depth * tPower * (20 + valence * 12 + profile.electronegativity() * 8);
            toughness += fraction * toughnessContribution * tPower;
            conductivity += fraction * (ChemistryRules.isMetal(element) ? 115 : 18)
                    / (1 + profile.electronegativity() * 0.22);
            heat += fraction * depth * profile.meltingPointK() * 0.08;
            corrosion += fraction * (18 + profile.electronegativity() * 17);
            density += fraction * profile.density();

            // Deeper fictional periods offer larger benefits but carry larger drawbacks.
            if (tMetal) {
                traits.add(TOrbitalTraits.trait(element));
            } else {
                brittleness += fraction * Math.max(0, depth * valence * 7 - toughnessContribution * 0.002);
                toxicity += fraction * Math.max(0, element.number() - 30) * depth * 0.12;
                instability += fraction * (element.number() > 82 ? 16 * depth : 0);
            }
        }

        for (int left = 0; left < constituents.size(); left++) {
            for (int right = left + 1; right < constituents.size(); right++) {
                Constituent a = constituents.get(left);
                Constituent b = constituents.get(right);
                ElementProfile ap = ElementProfile.calculate(a.element());
                ElementProfile bp = ElementProfile.calculate(b.element());
                double contact = Math.sqrt(a.percent() * b.percent()) / 100.0;
                double radiusMismatch = Math.abs(ap.atomicRadiusPm() - bp.atomicRadiusPm())
                        / Math.max(ap.atomicRadiusPm(), bp.atomicRadiusPm());
                double electronegativityDifference = Math.abs(ap.electronegativity() - bp.electronegativity());
                boolean pureTInteraction = a.element().block() == ElectronBlock.T
                        && b.element().block() == ElectronBlock.T;
                strength += contact * (radiusMismatch * 95 + electronegativityDifference * 28);
                hardness += contact * radiusMismatch * 110;
                corrosion += contact * electronegativityDifference * 22;
                if (!pureTInteraction) {
                    conductivity -= contact * radiusMismatch * 38;
                    brittleness += contact * Math.max(0, radiusMismatch - 0.12) * 70;
                }
            }
        }
        double attackSpeed = Math.max(0.5, Math.min(4.0,
                1.2 + conductivity / 90.0 + toughness / 350.0 - density / 45.0));
        return new AlloyProperties(round(strength), round(hardness), round(toughness), round(attackSpeed),
                round(Math.max(0, conductivity)), round(heat), round(corrosion), round(density),
                round(brittleness), round(toxicity), round(instability), List.copyOf(traits));
    }

    private static double round(double value) { return Math.round(value * 100.0) / 100.0; }

    public record Constituent(Element element, double percent) {
        public Constituent {
            if (percent <= 0 || !Double.isFinite(percent)) {
                throw new IllegalArgumentException("Concentration must be a positive finite percentage");
            }
        }
    }
}
