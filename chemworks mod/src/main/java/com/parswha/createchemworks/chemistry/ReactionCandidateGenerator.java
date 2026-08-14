package com.parswha.createchemworks.chemistry;

import java.util.ArrayList;
import java.util.List;

/** Exhaustively enumerates every selected reactant subset of size 2–9. */
public final class ReactionCandidateGenerator {
    private ReactionCandidateGenerator() { }

    public static List<Candidate> generate(List<Input> reactants, double temperatureK,
                                           double pressureKpa, int catalystCount) {
        if (reactants.size() < 2 || reactants.size() > 9) {
            throw new IllegalArgumentException("Reaction tester requires 2–9 reactants");
        }
        List<Candidate> results = new ArrayList<>((1 << reactants.size()) - reactants.size() - 1);
        int limit = 1 << reactants.size();
        for (int mask = 0; mask < limit; mask++) {
            if (Integer.bitCount(mask) < 2) continue;
            List<Input> selected = new ArrayList<>();
            for (int index = 0; index < reactants.size(); index++) {
                if ((mask & (1 << index)) != 0) selected.add(reactants.get(index));
            }
            var result = LiveReactionCalculator.calculate(
                    selected.stream().map(Input::formula).toList(), temperatureK, pressureKpa);
            double concentration = selected.stream().mapToDouble(Input::concentration).reduce(1, (a, b) -> a * b);
            ReactionKinetics kinetics = ReactionKinetics.estimate(result.reaction(), temperatureK,
                    pressureKpa, concentration, 1, catalystCount);
            results.add(new Candidate(selected, result, kinetics));
        }
        return List.copyOf(results);
    }

    public record Input(String formula, double concentration) {
        public Input {
            if (formula == null || formula.isBlank()) throw new IllegalArgumentException("Formula is required");
            if (!Double.isFinite(concentration) || concentration < 0) {
                throw new IllegalArgumentException("Concentration must be finite and nonnegative");
            }
        }
    }

    public record Candidate(List<Input> inputs, LiveReactionCalculator.Result result,
                            ReactionKinetics kinetics) {
        public Candidate { inputs = List.copyOf(inputs); }
    }
}
