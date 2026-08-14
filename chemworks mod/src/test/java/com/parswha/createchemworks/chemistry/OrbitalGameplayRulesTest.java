package com.parswha.createchemworks.chemistry;

import com.parswha.createchemworks.client.Elements.ElectronBlock;
import com.parswha.createchemworks.client.Elements.ElementsData;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrbitalGameplayRulesTest {
    @Test void pBlockIsNonmetalAndTBlockHasNoAlloyNegatives() {
        assertTrue(ElementsData.all().stream().filter(e -> e.block() == ElectronBlock.P).noneMatch(ChemistryRules::isMetal));
        var t = ElementsData.all().stream().filter(e -> e.block() == ElectronBlock.T).toList();
        double share = 100d / t.size();
        AlloyProperties alloy = AlloyCalculator.calculate(t.stream().map(e -> new AlloyCalculator.Constituent(e, share)).toList());
        assertEquals(0, alloy.brittleness()); assertEquals(0, alloy.toxicity()); assertEquals(0, alloy.instability());
        assertEquals(36, alloy.positiveTraits().size());
    }

    @Test void fOrbitalParticipationAcceleratesOtherwiseEquivalentReaction() {
        int f = ElementsData.all().stream().filter(e -> e.block() == ElectronBlock.F).findFirst().orElseThrow().number();
        ChemicalReaction normal = reaction(List.of(1, 8));
        ChemicalReaction reactive = reaction(List.of(1, f));
        double normalRate = ReactionKinetics.estimate(normal, 600, 101.325, 1, 1, 0).forwardRate();
        double reactiveRate = ReactionKinetics.estimate(reactive, 600, 101.325, 1, 1, 0).forwardRate();
        assertTrue(reactiveRate > normalRate * 6);
    }

    private static ChemicalReaction reaction(List<Integer> elements) {
        return new ChemicalReaction(ResourceLocation.fromNamespaceAndPath("test", "reaction"), "A + B → AB",
                elements, 300, 101.325, -50, "None", "test");
    }
}
