package com.parswha.createchemworks.chemistry;

import com.parswha.createchemworks.CreateChemworks;
import com.parswha.createchemworks.client.Elements.Element;
import com.parswha.createchemworks.client.Elements.ElementsData;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/** Deterministic five-pass chemistry generator and runtime catalogue. */
public final class ReactionCalculator {
    public static final int GENERATOR_VERSION = 9;
    public static final int PASSES = 10;
    public static final int REACTIONS_PER_PASS = 100_000;
    private static volatile List<ChemicalReaction> reactions = List.of();
    private static volatile Consumer<List<ChemicalReaction>> updateListener = ignored -> { };
    private static final List<ChemicalReaction> incoming = new ArrayList<>();

    private ReactionCalculator() { }

    public static List<ChemicalReaction> generate() {
        List<Element> elements = ElementsData.all();
        List<ChemicalReaction> generated = new ArrayList<>(PASSES * REACTIONS_PER_PASS);
        generated.addAll(knownFuelReactions());
        List<Species> frontier = new ArrayList<>();
        Set<String> knownSpecies = new HashSet<>();

        outer:
        for (int left = 0; left < elements.size(); left++) {
            for (int right = left + 1; right < elements.size(); right++) {
                Element a = elements.get(left);
                Element b = elements.get(right);
                if (!canBond(a, b)) continue;
                Species product = Species.binary(a, b);
                if (!knownSpecies.add(product.key())) continue;
                frontier.add(product);
                generated.add(reaction(generated.size(), 1,
                        binaryFormationEquation(a, b, product),
                        List.of(a.number(), b.number())));
                if (frontier.size() >= REACTIONS_PER_PASS) break outer;
            }
        }

        for (int pass = 2; pass <= PASSES; pass++) {
            List<Species> next = new ArrayList<>(REACTIONS_PER_PASS);
            for (Species species : frontier) {
                int start = Math.floorMod(species.key().hashCode(), elements.size());
                int accepted = 0;
                for (int offset = 0; offset < elements.size() && accepted < 10; offset++) {
                    Element element = elements.get((start + offset) % elements.size());
                    if (!canExtend(species, element)) continue;
                    Species product = species.with(element);
                    if (!knownSpecies.add(product.key())) continue;
                    next.add(product);
                    generated.add(reaction(generated.size(), pass,
                            extensionEquation(species, element, product),
                            mergeParticipants(species.atomicNumbers(), element.number())));
                    accepted++;
                    if (next.size() >= REACTIONS_PER_PASS) break;
                }
                if (next.size() >= REACTIONS_PER_PASS) break;
            }
            frontier = next;
            if (frontier.isEmpty()) break;
        }
        return List.copyOf(generated);
    }

    private static List<ChemicalReaction> knownFuelReactions() {
        List<ChemicalReaction> known = new ArrayList<>();
        for (KnownCompound compound : KnownCompounds.ALL) {
            if (!compound.isFuel()) continue;
            String equation = combustionEquation(compound);
            if (equation == null) continue;
            List<Integer> participants = new HashSet<>(compound.composition().keySet()).stream().sorted().toList();
            if (!participants.contains(8)) participants = mergeParticipants(participants, 8);
            known.add(new ChemicalReaction(ResourceLocation.fromNamespaceAndPath(CreateChemworks.MOD_ID,
                    "known_combustion_" + compound.id()), equation, participants,
                    compound.ignitionTemperatureK(), 101.325,
                    -Math.round(compound.energyDensityMjKg() * compound.molarMass() * 10.0) / 10.0,
                    "Ignition source", "Curated real-world fuel baseline"));
        }
        return known;
    }

    private static String combustionEquation(KnownCompound compound) {
        if (compound.id().equals("hydrogen")) return "2 H₂ + O₂ → 2 H₂O";
        if (compound.id().equals("ammonia")) return "4 NH₃ + 3 O₂ → 2 N₂ + 6 H₂O";
        if (compound.id().equals("hydrazine")) return "N₂H₄ + O₂ → N₂ + 2 H₂O";
        int carbon = compound.composition().getOrDefault(6, 0);
        int hydrogen = compound.composition().getOrDefault(1, 0);
        int oxygen = compound.composition().getOrDefault(8, 0);
        if (carbon == 0) return null;
        int oxygenCoefficient = 2 * carbon + hydrogen / 2 - oxygen;
        return "2 " + compound.formula() + " + " + oxygenCoefficient + " O₂ → "
                + 2 * carbon + " CO₂" + (hydrogen == 0 ? "" : " + " + hydrogen + " H₂O");
    }

    private static String binaryFormationEquation(Element a, Element b, Species product) {
        int productA = product.counts().get(a.number());
        int productB = product.counts().get(b.number());
        int moleculeA = ChemistryRules.elementalMoleculeSize(a);
        int moleculeB = ChemistryRules.elementalMoleculeSize(b);
        int productCoefficient = lcm(moleculeA / gcd(moleculeA, productA),
                moleculeB / gcd(moleculeB, productB));
        int coefficientA = productCoefficient * productA / moleculeA;
        int coefficientB = productCoefficient * productB / moleculeB;
        return coefficient(coefficientA) + ChemistryRules.elementalFormula(a) + " + "
                + coefficient(coefficientB) + ChemistryRules.elementalFormula(b) + " → "
                + coefficient(productCoefficient) + product.formula();
    }

    private static String extensionEquation(Species reactant, Element element, Species product) {
        int moleculeSize = ChemistryRules.elementalMoleculeSize(element);
        if (moleculeSize == 1) {
            return reactant.formula() + " + " + ChemistryRules.elementalFormula(element)
                    + " → " + product.formula();
        }
        return "2 " + reactant.formula() + " + " + ChemistryRules.elementalFormula(element)
                + " → 2 " + product.formula();
    }

    private static String coefficient(int value) { return value == 1 ? "" : value + " "; }

    public static List<ChemicalReaction> reactions() { return reactions; }

    public static synchronized void replace(List<ChemicalReaction> catalogue) {
        reactions = List.copyOf(catalogue);
        updateListener.accept(reactions);
    }

    public static void onCatalogueChanged(Consumer<List<ChemicalReaction>> listener) {
        updateListener = listener;
    }

    public static synchronized void acceptServerBatch(boolean reset, boolean last,
                                                       List<ChemicalReaction> batch) {
        if (reset) incoming.clear();
        incoming.addAll(batch);
        if (last) replace(List.copyOf(incoming));
    }

    public static List<ChemicalReaction> involving(int atomicNumber) {
        return reactions.stream()
                .filter(reaction -> reaction.participatingElements().contains(atomicNumber))
                .toList();
    }

    private static boolean canBond(Element a, Element b) {
        if (ChemistryRules.isNobleGas(a) || ChemistryRules.isNobleGas(b)) return false;
        return !(ChemistryRules.isMetal(a) && ChemistryRules.isMetal(b));
    }

    private static boolean canExtend(Species species, Element element) {
        if (ChemistryRules.isNobleGas(element) || species.counts().containsKey(element.number())) return false;
        boolean containsMetal = species.atomicNumbers().stream()
                .map(number -> ElementsData.all().get(number - 1)).anyMatch(ChemistryRules::isMetal);
        return !containsMetal || !ChemistryRules.isMetal(element);
    }

    private static ChemicalReaction reaction(int index, int pass, String equation,
                                               List<Integer> participants) {
        ThermodynamicEstimator.Conditions conditions =
                ThermodynamicEstimator.estimate(participants, pass, equation);
        return new ChemicalReaction(ResourceLocation.fromNamespaceAndPath(CreateChemworks.MOD_ID,
                "generated_" + pass + "_" + index), equation, participants,
                conditions.temperatureK(), conditions.pressureKpa(), conditions.enthalpyKj(),
                "None", conditions.confidence() + " (pass " + pass + ")");
    }

    private static List<Integer> mergeParticipants(List<Integer> values, int number) {
        Set<Integer> merged = new HashSet<>(values);
        merged.add(number);
        return merged.stream().sorted().toList();
    }

    private record Species(Map<Integer, Integer> counts, String formula, String key,
                           List<Integer> atomicNumbers) {
        static Species binary(Element a, Element b) {
            Map<Integer, Integer> counts = new LinkedHashMap<>();
            int aValence = Math.max(1, ChemistryRules.valenceElectrons(a));
            int bValence = Math.max(1, 8 - Math.min(7, ChemistryRules.valenceElectrons(b)));
            int divisor = gcd(aValence, bValence);
            counts.put(a.number(), Math.max(1, bValence / divisor));
            counts.put(b.number(), Math.max(1, aValence / divisor));
            return create(counts);
        }

        Species with(Element element) {
            Map<Integer, Integer> result = new LinkedHashMap<>(counts);
            result.put(element.number(), 1 + result.getOrDefault(element.number(), 0));
            return create(result);
        }

        private static Species create(Map<Integer, Integer> input) {
            Map<Integer, Integer> sorted = new LinkedHashMap<>();
            input.entrySet().stream().sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> sorted.put(entry.getKey(), entry.getValue()));
            StringBuilder formula = new StringBuilder();
            StringBuilder key = new StringBuilder();
            for (var entry : sorted.entrySet()) {
                Element element = ElementsData.all().get(entry.getKey() - 1);
                formula.append(element.symbol());
                if (entry.getValue() > 1) formula.append(entry.getValue());
                key.append(entry.getKey()).append(':').append(entry.getValue()).append(';');
            }
            return new Species(Map.copyOf(sorted), formula.toString(), key.toString(), List.copyOf(sorted.keySet()));
        }
    }

    private static int gcd(int a, int b) { return b == 0 ? a : gcd(b, a % b); }
    private static int lcm(int a, int b) { return a / gcd(a, b) * b; }
}
