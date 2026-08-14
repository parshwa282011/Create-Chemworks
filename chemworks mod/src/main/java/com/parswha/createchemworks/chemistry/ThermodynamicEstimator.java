package com.parswha.createchemworks.chemistry;

import com.parswha.createchemworks.client.Elements.Element;
import com.parswha.createchemworks.client.Elements.ElementsData;

import java.util.List;

/**
 * Fast group-contribution estimate for generated chemistry. Values are
 * physically motivated predictions, not replacements for measured data.
 */
public final class ThermodynamicEstimator {
    private ThermodynamicEstimator() { }

    public static Conditions estimate(List<Integer> atomicNumbers, int pass, String equation) {
        List<Element> elements = atomicNumbers.stream()
                .map(number -> ElementsData.all().get(number - 1)).toList();
        List<ElementProfile> profiles = elements.stream().map(ElementProfile::calculate).toList();

        double formedBondEnergy = 0;
        int pairs = 0;
        for (int left = 0; left < elements.size(); left++) {
            for (int right = left + 1; right < elements.size(); right++) {
                formedBondEnergy += estimatedBondEnergy(elements.get(left), profiles.get(left),
                        elements.get(right), profiles.get(right));
                pairs++;
            }
        }
        if (pairs == 0) formedBondEnergy = 100;
        else formedBondEnergy /= pairs;
        formedBondEnergy *= 1.0 + (pass - 1) * 0.16;

        double structuralReorganization = pass == 1 ? 0 : formedBondEnergy
                * (0.38 + unitVariation(equation, 17) * 0.42);
        double phaseCost = profiles.stream()
                .mapToDouble(profile -> Math.max(0, profile.meltingPointK() - 298.15) * 0.018)
                .sum() / Math.max(1, profiles.size());
        double entropyContribution = (unitVariation(equation, 31) - 0.5)
                * (35 + 12 * elements.size());
        double enthalpy = structuralReorganization + phaseCost + entropyContribution - formedBondEnergy;

        double activationEnergy = 35 + formedBondEnergy * (0.18 + 0.28 * unitVariation(equation, 47))
                + phaseCost * 0.7 + (pass - 1) * 18;
        double temperature = 273.15 + activationEnergy * 3.1;
        if (elements.stream().anyMatch(Element::acceleratorOnly)) temperature += 650 + pass * 90;
        temperature = clamp(temperature, 250, 4200);

        long gases = profiles.stream().filter(profile -> profile.standardPhase().equals("Gas")).count();
        double pressure;
        if (gases == 0) {
            pressure = 101.325 + unitVariation(equation, 61) * 80;
        } else {
            pressure = 101.325 * (1 + gases * (0.8 + unitVariation(equation, 73) * 3.5));
        }
        if (elements.stream().anyMatch(Element::acceleratorOnly)) pressure *= 2.5 + pass * 0.35;
        pressure = clamp(pressure, 0.01, 5000);

        String confidence = elements.stream().allMatch(element -> element.number() <= 118)
                ? "Estimated from periodic trends; measured override recommended"
                : "Predicted extended-element thermodynamics";
        return new Conditions(round(temperature), round(pressure), round(enthalpy), confidence);
    }

    private static double estimatedBondEnergy(Element a, ElementProfile aProfile,
                                              Element b, ElementProfile bProfile) {
        double electronegativityDifference = Math.abs(
                aProfile.electronegativity() - bProfile.electronegativity());
        double valenceFactor = Math.sqrt(Math.max(1,
                aProfile.valenceElectrons() * bProfile.valenceElectrons()));
        double radiusFactor = 1800.0 / Math.max(35,
                (aProfile.atomicRadiusPm() + bProfile.atomicRadiusPm()) / 2.0);
        double ionicContribution = electronegativityDifference > 1.7
                ? 170 * electronegativityDifference : 55 * electronegativityDifference;
        double orbitalContribution = a.block() == b.block() ? 22 : 48;
        return clamp(85 + valenceFactor * 34 + radiusFactor + ionicContribution
                + orbitalContribution, 80, 1200);
    }

    private static double unitVariation(String value, int salt) {
        long mixed = Integer.toUnsignedLong(value.hashCode() * 31 + salt * 0x9E3779B9);
        return (mixed % 10_000) / 9_999.0;
    }

    private static double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private static double round(double value) { return Math.round(value * 1000.0) / 1000.0; }

    public record Conditions(double temperatureK, double pressureKpa,
                             double enthalpyKj, String confidence) { }
}
