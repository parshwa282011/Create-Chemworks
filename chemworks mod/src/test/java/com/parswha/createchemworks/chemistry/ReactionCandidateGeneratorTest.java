package com.parswha.createchemworks.chemistry;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class ReactionCandidateGeneratorTest {
    private static final List<String> FORMULAS = List.of("H", "O", "C", "N", "S", "Cl", "Fe", "Cu", "Si");

    @Test
    void nineInputsProduceEverySubsetOfSizeTwoThroughNine() {
        var inputs = IntStream.range(0, 9)
                .mapToObj(i -> new ReactionCandidateGenerator.Input(FORMULAS.get(i), 1)).toList();
        var results = ReactionCandidateGenerator.generate(inputs, 1200, 500, 0);
        assertEquals(502, results.size());
        for (int size = 2; size <= 9; size++) {
            int expectedSize = size;
            int expected = binomial(9, size);
            assertEquals(expected, results.stream().filter(candidate -> candidate.inputs().size() == expectedSize).count());
        }
    }

    @Test
    void rejectsOutsideTesterCapacity() {
        assertThrows(IllegalArgumentException.class,
                () -> ReactionCandidateGenerator.generate(List.of(new ReactionCandidateGenerator.Input("H", 1)), 300, 101, 0));
    }

    private static int binomial(int n, int k) {
        int result = 1;
        for (int i = 1; i <= k; i++) result = result * (n - i + 1) / i;
        return result;
    }
}
