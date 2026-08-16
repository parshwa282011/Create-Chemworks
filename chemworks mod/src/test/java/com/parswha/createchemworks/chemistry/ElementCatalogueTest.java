package com.parswha.createchemworks.chemistry;

import com.parswha.createchemworks.client.Elements.ElectronBlock;
import com.parswha.createchemworks.client.Elements.ElementsData;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class ElementCatalogueTest {
    @Test
    void catalogueHasExactly218ElementsAndFiveOrbitals() {
        assertEquals(218, ElementsData.all().size());
        assertEquals(EnumSet.of(ElectronBlock.S, ElectronBlock.P, ElectronBlock.D,
                ElectronBlock.F, ElectronBlock.T), EnumSet.allOf(ElectronBlock.class));
        assertEquals(36, ElementsData.elementsIn(ElectronBlock.T).size());
        assertEquals(18, ElementsData.elementsIn(ElectronBlock.T).stream().filter(e -> e.period() == 8).count());
        assertEquals(18, ElementsData.elementsIn(ElectronBlock.T).stream().filter(e -> e.period() == 9).count());
    }

    @Test
    void everyTMetalHasOneUniquePositiveTraitAndNoNaturalSource() {
        var tMetals = ElementsData.elementsIn(ElectronBlock.T);
        var traits = tMetals.stream().map(TOrbitalTraits::trait).toList();
        assertEquals(36, new HashSet<>(traits).size());
        assertTrue(tMetals.stream().allMatch(ChemistryRules::isMetal));
        assertTrue(tMetals.stream().allMatch(element -> element.acceleratorOnly()));
    }

    @Test
    void completeTAlloyHasEveryTraitAndNoNegativeProperty() {
        var tMetals = ElementsData.elementsIn(ElectronBlock.T);
        double share = 100.0 / tMetals.size();
        var alloy = AlloyCalculator.calculate(tMetals.stream()
                .map(element -> new AlloyCalculator.Constituent(element, share)).toList());
        assertEquals(36, alloy.positiveTraits().size());
        assertEquals(0, alloy.brittleness());
        assertEquals(0, alloy.toxicity());
        assertEquals(0, alloy.instability());
        assertTrue(alloy.attackSpeed() >= 0.5 && alloy.attackSpeed() <= 4.0);
    }

    @Test
    void survivalCalibrationSetCoversEveryTierExactly() {
        for (int tier = 1; tier <= 7; tier++) {
            assertEquals(tier, com.parswha.createchemworks.integration.tetra.ChemworksTetraIntegration
                    .alloyTier(AlloyCommand.calibrationAlloy(tier)));
        }
    }
}
