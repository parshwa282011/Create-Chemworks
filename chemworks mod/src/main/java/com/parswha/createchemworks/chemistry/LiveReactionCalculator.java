package com.parswha.createchemworks.chemistry;

import com.parswha.createchemworks.CreateChemworks;
import com.parswha.createchemworks.client.Elements.Element;
import com.parswha.createchemworks.client.Elements.ElementsData;
import net.minecraft.resources.ResourceLocation;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Calculates one requested reaction. It deliberately does no catalogue-wide generation. */
public final class LiveReactionCalculator {
    private LiveReactionCalculator() { }

    public static Result calculate(String leftInput, String rightInput,
                                   double temperatureK, double pressureKpa) {
        boolean leftElectron = isElectron(leftInput), rightElectron = isElectron(rightInput);
        if (leftElectron || rightElectron) {
            if (leftElectron && rightElectron) throw new IllegalArgumentException("Two electrons alone do not form a chemical product");
            Species species = parse(leftElectron ? rightInput : leftInput);
            CalculatedCompound neutral = properties(species.composition());
            CalculatedCompound product = new CalculatedCompound(neutral.formula() + "⁻", neutral.composition(),
                    neutral.molarMass(), neutral.bondType(), neutral.phase(), neutral.meltingPointK(),
                    neutral.boilingPointK(), "Estimated one-electron reduction; net charge -1");
            List<Integer> participants = species.composition().keySet().stream().sorted().toList();
            String equation = species.formula() + " + e⁻ → " + product.formula();
            ThermodynamicEstimator.Conditions estimate = ThermodynamicEstimator.estimate(participants, 1, equation);
            ChemicalReaction reaction = new ChemicalReaction(ResourceLocation.fromNamespaceAndPath(CreateChemworks.MOD_ID,
                    "live_" + UUID.nameUUIDFromBytes(equation.getBytes(StandardCharsets.UTF_8)).toString().replace("-", "")),
                    equation, participants, estimate.temperatureK(), estimate.pressureKpa(), estimate.enthalpyKj(),
                    "Electron transfer", estimate.confidence());
            return new Result(reaction, product, temperatureK >= estimate.temperatureK());
        }
        Species left = parse(leftInput);
        Species right = parse(rightInput);
        Map<Integer, Integer> productComposition = binaryProduct(left, right);
        CalculatedCompound product = properties(productComposition);
        List<Integer> participants = productComposition.keySet().stream().sorted().toList();
        String equation = left.formula() + " + " + right.formula() + " → " + product.formula();
        ThermodynamicEstimator.Conditions estimate = ThermodynamicEstimator.estimate(participants, 1, equation);
        boolean conditionsMet = temperatureK >= estimate.temperatureK()
                && pressureKpa >= Math.min(estimate.pressureKpa(), 101.325);
        String key = equation;
        String id = "live_" + UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8)).toString().replace("-", "");
        ChemicalReaction reaction = new ChemicalReaction(ResourceLocation.fromNamespaceAndPath(
                CreateChemworks.MOD_ID, id), equation, participants, estimate.temperatureK(),
                estimate.pressureKpa(), estimate.enthalpyKj(), "None", estimate.confidence());
        return new Result(reaction, product, conditionsMet);
    }

    /** Calculates one deterministic gameplay product from two through nine simultaneous reactants. */
    public static Result calculate(List<String> inputs, double temperatureK, double pressureKpa) {
        if (inputs.size() < 2 || inputs.size() > 9) {
            throw new IllegalArgumentException("A reaction requires 2–9 reactants");
        }
        if (inputs.size() == 2) return calculate(inputs.get(0), inputs.get(1), temperatureK, pressureKpa);
        Map<Integer, Integer> composition = new LinkedHashMap<>();
        List<String> formulas = new ArrayList<>();
        for (String input : inputs) {
            if (isElectron(input)) throw new IllegalArgumentException("Electrons are only supported in two-input reactions");
            Species species = parse(input);
            formulas.add(species.formula());
            species.composition().forEach((number, count) -> composition.merge(number, count, Integer::sum));
        }
        Map<Integer, Integer> reducedComposition = reduce(composition);
        CalculatedCompound product = properties(reducedComposition);
        List<Integer> participants = reducedComposition.keySet().stream().sorted().toList();
        String equation = String.join(" + ", formulas) + " → " + product.formula();
        ThermodynamicEstimator.Conditions estimate = ThermodynamicEstimator.estimate(participants,
                inputs.size() - 1, equation);
        boolean conditionsMet = temperatureK >= estimate.temperatureK()
                && pressureKpa >= Math.min(estimate.pressureKpa(), 101.325);
        String id = "live_" + UUID.nameUUIDFromBytes(equation.getBytes(StandardCharsets.UTF_8))
                .toString().replace("-", "");
        ChemicalReaction reaction = new ChemicalReaction(ResourceLocation.fromNamespaceAndPath(
                CreateChemworks.MOD_ID, id), equation, participants, estimate.temperatureK(),
                estimate.pressureKpa(), estimate.enthalpyKj(), "None", estimate.confidence());
        return new Result(reaction, product, conditionsMet);
    }

    private static boolean isElectron(String input) {
        String value = input.trim();
        return value.equalsIgnoreCase("e_-") || value.equals("e⁻");
    }

    private static Species parse(String input) {
        KnownCompound known = KnownCompounds.find(input);
        if (known != null) return new Species(known.formula(), known.composition());
        String formula = input.trim();
        Map<Integer, Integer> composition = new LinkedHashMap<>();
        for (int cursor = 0; cursor < formula.length();) {
            if (!Character.isUpperCase(formula.charAt(cursor)))
                throw new IllegalArgumentException("Unknown compound or invalid formula: " + input);
            int symbolEnd = cursor + 1;
            if (symbolEnd < formula.length() && Character.isLowerCase(formula.charAt(symbolEnd))) symbolEnd++;
            String symbol = formula.substring(cursor, symbolEnd);
            int numberEnd = symbolEnd;
            while (numberEnd < formula.length() && Character.isDigit(formula.charAt(numberEnd))) numberEnd++;
            int count = numberEnd == symbolEnd ? 1 : Integer.parseInt(formula.substring(symbolEnd, numberEnd));
            Element element = ElementsData.all().stream().filter(candidate -> candidate.symbol().equals(symbol))
                    .findFirst().orElseThrow(() -> new IllegalArgumentException("Unknown element symbol: " + symbol));
            composition.merge(element.number(), count, Integer::sum);
            cursor = numberEnd;
        }
        if (composition.isEmpty()) throw new IllegalArgumentException("Empty formula");
        return new Species(formula, Map.copyOf(composition));
    }

    private static CalculatedCompound properties(Map<Integer, Integer> composition) {
        double mass = 0, melting = 0, boiling = 0;
        int atoms = 0;
        boolean metal = false, nonMetal = false;
        StringBuilder formula = new StringBuilder();
        for (var entry : composition.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
            Element element = ElementsData.all().get(entry.getKey() - 1);
            ElementProfile profile = ElementProfile.calculate(element);
            formula.append(element.symbol());
            if (entry.getValue() > 1) formula.append(entry.getValue());
            mass += profile.atomicMass() * entry.getValue();
            melting += profile.meltingPointK() * entry.getValue();
            boiling += profile.boilingPointK() * entry.getValue();
            atoms += entry.getValue();
            if (ChemistryRules.isMetal(element)) metal = true; else nonMetal = true;
        }
        String bond = metal && nonMetal ? "Ionic" : metal ? "Metallic" : "Covalent";
        double bondFactor = bond.equals("Ionic") ? 1.35 : bond.equals("Metallic") ? 1.15 : 0.82;
        melting = melting / atoms * bondFactor;
        boiling = Math.max(melting + 100, boiling / atoms * bondFactor);
        String phase = boiling < 298.15 ? "Gas" : melting > 298.15 ? "Solid" : "Liquid";
        return new CalculatedCompound(formula.toString(), Map.copyOf(composition), round(mass), bond,
                phase, round(melting), round(boiling), "Estimated from constituent element profiles");
    }

    private static Map<Integer, Integer> reduce(Map<Integer, Integer> values) {
        int divisor = 0;
        for (int value : values.values()) divisor = gcd(divisor, value);
        if (divisor <= 1) return Map.copyOf(values);
        Map<Integer, Integer> result = new LinkedHashMap<>();
        for (var entry : values.entrySet()) result.put(entry.getKey(), entry.getValue() / divisor);
        return Map.copyOf(result);
    }

    private static Map<Integer, Integer> binaryProduct(Species left, Species right) {
        if (left.composition().size() == 1 && right.composition().size() == 1) {
            int leftNumber = left.composition().keySet().iterator().next();
            int rightNumber = right.composition().keySet().iterator().next();
            if (leftNumber != rightNumber) {
                Element a = ElementsData.all().get(leftNumber - 1);
                Element b = ElementsData.all().get(rightNumber - 1);
                int aBond = bondingCapacity(a);
                int bBond = bondingCapacity(b);
                int divisor = gcd(aBond, bBond);
                Map<Integer, Integer> result = new LinkedHashMap<>();
                result.put(leftNumber, bBond / divisor);
                result.put(rightNumber, aBond / divisor);
                return Map.copyOf(result);
            }
        }
        Map<Integer, Integer> result = new LinkedHashMap<>(left.composition());
        for (var entry : right.composition().entrySet())
            result.merge(entry.getKey(), entry.getValue(), Integer::sum);
        return reduce(result);
    }

    private static int bondingCapacity(Element element) {
        int valence = ChemistryRules.valenceElectrons(element);
        return Math.max(1, valence <= 4 ? valence : 8 - valence);
    }

    private static int gcd(int a, int b) { return b == 0 ? a : gcd(b, a % b); }
    private static double round(double value) { return Math.round(value * 1000.0) / 1000.0; }
    private record Species(String formula, Map<Integer, Integer> composition) { }
    public record Result(ChemicalReaction reaction, CalculatedCompound product, boolean conditionsMet) { }
}
