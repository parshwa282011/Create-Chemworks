package com.parswha.createchemworks.machinery;

import java.util.Objects;

/** A single-species, pressurised chemical volume. Amount is measured in mB. */
public final class ChemicalVolume {
    public static final double AMBIENT_K = 293.15;
    private final long capacity;
    private String chemical = "";
    private long amount;
    private double temperatureK = AMBIENT_K;
    private double pumpPressureKpa;

    public ChemicalVolume(long capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("capacity must be positive");
        this.capacity = capacity;
    }

    public long fill(String type, long requested, double incomingTemperatureK) {
        Objects.requireNonNull(type, "type");
        if (type.isBlank() || requested <= 0 || !Double.isFinite(incomingTemperatureK) || incomingTemperatureK <= 0) return 0;
        if (amount > 0 && !chemical.equals(type)) return 0;
        long accepted = Math.min(requested, capacity - amount);
        if (accepted == 0) return 0;
        temperatureK = (temperatureK * amount + incomingTemperatureK * accepted) / (amount + accepted);
        chemical = type;
        amount += accepted;
        return accepted;
    }

    public long drain(long requested) {
        long drained = Math.min(Math.max(0, requested), amount);
        amount -= drained;
        if (amount == 0) chemical = "";
        return drained;
    }

    public long transferTo(ChemicalVolume target, long requested) {
        if (amount == 0) return 0;
        long moved = target.fill(chemical, Math.min(amount, requested), temperatureK);
        drain(moved);
        return moved;
    }

    /** Gameplay pressure model: ideal-gas-like fill pressure plus applied pump force. */
    public double pressureKpa() {
        return amount == 0 ? 0 : 101.325 * ((double) amount / capacity) * (temperatureK / AMBIENT_K) + pumpPressureKpa;
    }

    public void approachTemperature(double targetK, double energyFactor) {
        if (!Double.isFinite(targetK) || targetK <= 0 || energyFactor <= 0) return;
        temperatureK += (targetK - temperatureK) * Math.min(1, energyFactor);
    }

    public long capacity() { return capacity; }
    public String chemical() { return chemical; }
    public long amount() { return amount; }
    public double temperatureK() { return temperatureK; }
    public double pumpPressureKpa() { return pumpPressureKpa; }
    public void setPumpPressureKpa(double value) { pumpPressureKpa = Math.max(0, value); }
    public void decayPumpPressure(double fraction) { pumpPressureKpa *= Math.max(0, 1 - fraction); }
    public boolean transform(String expected, String product) {
        if (amount == 0 || !chemical.replace(" ", "").equals(expected.replace(" ", "")) || product.isBlank()) return false;
        chemical = product.trim(); return true;
    }
}
