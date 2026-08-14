package com.parswha.createchemworks.machinery;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ChemicalVolumeTest {
    @Test void enforcesSingleChemicalAndCapacity() {
        ChemicalVolume tank = new ChemicalVolume(1000);
        assertEquals(800, tank.fill("N2", 800, 300));
        assertEquals(0, tank.fill("O2", 100, 300));
        assertEquals(200, tank.fill("N2", 500, 300));
        assertEquals(1000, tank.amount());
    }

    @Test void transferIsConservativeAndPressureUsesVolumeTemperatureAndPumps() {
        ChemicalVolume source = new ChemicalVolume(1000);
        ChemicalVolume target = new ChemicalVolume(2000);
        source.fill("H2", 900, 400);
        assertEquals(600, source.transferTo(target, 600));
        assertEquals(300, source.amount());
        assertEquals(600, target.amount());
        double unboosted = target.pressureKpa();
        target.setPumpPressureKpa(250);
        assertEquals(unboosted + 250, target.pressureKpa(), 0.001);
    }
}
