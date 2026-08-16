package com.parswha.createchemworks.machinery;

import java.util.Objects;

/** A single-species, pressurised chemical volume. One internal unit is one millimole. */
public final class ChemicalVolume {
    public static final double AMBIENT_K = 293.15;
    public static final double GAS_CONSTANT_KPA_L_PER_MOL_K = 8.314462618;
    private final long physicalVolumeUnits;
    private final long capacity;
    private String chemical = "";
    private long amount;
    private double temperatureK = AMBIENT_K;

    public ChemicalVolume(long capacity) {
        this(capacity, 1);
    }

    public ChemicalVolume(long physicalVolumeUnits, int maximumCompressionRatio) {
        if (physicalVolumeUnits <= 0) throw new IllegalArgumentException("volume must be positive");
        if (maximumCompressionRatio <= 0) throw new IllegalArgumentException("compression ratio must be positive");
        this.physicalVolumeUnits = physicalVolumeUnits;
        this.capacity = Math.multiplyExact(physicalVolumeUnits, maximumCompressionRatio);
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

    /** Ideal-gas pressure. Pumps affect pressure by moving moles into this fixed volume. */
    public double pressureKpa() {
        return amount == 0 ? 0 : moles() * GAS_CONSTANT_KPA_L_PER_MOL_K * temperatureK / volumeLiters();
    }

    public void approachTemperature(double targetK, double energyFactor) {
        if (!Double.isFinite(targetK) || targetK <= 0 || energyFactor <= 0) return;
        temperatureK += (targetK - temperatureK) * Math.min(1, energyFactor);
    }

    public long capacity() { return capacity; }
    public double moles() { return amount / 1_000.0; }
    public double maxMoles() { return capacity / 1_000.0; }
    public double volumeLiters() { return physicalVolumeUnits / 1_000.0; }
    public String chemical() { return chemical; }
    public long amount() { return amount; }
    public double temperatureK() { return temperatureK; }
    /** Kept for old saves/API callers; pressure is no longer stored independently. */
    @Deprecated public double pumpPressureKpa() { return 0; }
    @Deprecated public void setPumpPressureKpa(double value) { }
    @Deprecated public void decayPumpPressure(double fraction) { }
    public boolean transform(String expected, String product) {
        if (amount == 0 || !chemical.replace(" ", "").equals(expected.replace(" ", "")) || product.isBlank()) return false;
        chemical = product.trim(); return true;
    }
}
