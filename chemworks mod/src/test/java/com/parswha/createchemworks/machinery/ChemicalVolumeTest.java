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

    @Test void transferIsConservativeAndPressureUsesMolesVolumeAndTemperature() {
        ChemicalVolume source = new ChemicalVolume(1000);
        ChemicalVolume target = new ChemicalVolume(2000);
        source.fill("H2", 900, 400);
        assertEquals(600, source.transferTo(target, 600));
        assertEquals(300, source.amount());
        assertEquals(600, target.amount());
        assertEquals(0.6, target.moles(), 0.000_001);
        assertEquals(2.0, target.volumeLiters(), 0.000_001);
        double expected = 0.6 * ChemicalVolume.GAS_CONSTANT_KPA_L_PER_MOL_K * 400 / 2.0;
        assertEquals(expected, target.pressureKpa(), 0.001);
        double unboosted = target.pressureKpa();
        target.setPumpPressureKpa(250);
        assertEquals(unboosted, target.pressureKpa(), 0.001,
                "pump pressure is produced by transferred moles, never an artificial pressure field");
    }

    @Test void mixingConservesThermalEnergyByAmount() {
        ChemicalVolume tank = new ChemicalVolume(2_000);
        tank.fill("N2", 500, 200);
        tank.fill("N2", 1_500, 400);
        assertEquals(350, tank.temperatureK(), 0.000_001);
        assertEquals(2.0, tank.moles(), 0.000_001);
    }

    @Test void compressedVolumeRaisesPressureAsPumpsAddMoles() {
        ChemicalVolume tank = new ChemicalVolume(1_000, 8);
        tank.fill("H2", 1_000, 300);
        double oneMolePressure = tank.pressureKpa();
        tank.fill("H2", 1_000, 300);
        assertEquals(2 * oneMolePressure, tank.pressureKpa(), 0.001);
        assertEquals(1.0, tank.volumeLiters(), 0.000_001);
        assertEquals(8.0, tank.maxMoles(), 0.000_001);
    }
}
