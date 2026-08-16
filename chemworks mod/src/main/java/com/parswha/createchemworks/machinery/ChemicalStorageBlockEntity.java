package com.parswha.createchemworks.machinery;

import com.parswha.createchemworks.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class ChemicalStorageBlockEntity extends BlockEntity implements ChemicalNode {
    private final ChemicalVolume volume;
    private static final int ENERGY_CAPACITY = 100_000;
    private static final int MAX_RECEIVE = 2_000;
    private static final double MIN_TEMPERATURE_K = 1;
    private static final double MAX_TEMPERATURE_K = 5_000;
    private double targetTemperatureK = ChemicalVolume.AMBIENT_K;
    private final EnergyStorage energy = new EnergyStorage(ENERGY_CAPACITY, MAX_RECEIVE, 0);

    public ChemicalStorageBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.CHEMICAL_STORAGE.get(), pos, state,
                state.getBlock() instanceof ChemicalValveBlock ? 1_000 : 16_000);
    }

    protected ChemicalStorageBlockEntity(net.minecraft.world.level.block.entity.BlockEntityType<?> type,
            BlockPos pos, BlockState state, long capacity) {
        super(type, pos, state);
        // Chemical infrastructure can hold up to eight mol/L, allowing pumps in
        // parallel to genuinely compress a fixed physical volume.
        volume = new ChemicalVolume(capacity, 8);
    }

    @Override public ChemicalVolume chemicalVolume() { return volume; }
    @Override public boolean permitsFlow() {
        return !getBlockState().hasProperty(ChemicalValveBlock.OPEN)
                || getBlockState().getValue(ChemicalValveBlock.OPEN);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ChemicalStorageBlockEntity self) {
        self.regulateTemperature();
        self.equalizeNetwork();
    }

    private void regulateTemperature() {
        double difference = targetTemperatureK - volume.temperatureK();
        if (Math.abs(difference) < 0.05) return;
        int required = Math.max(1, Math.min(200, (int) Math.ceil(Math.abs(difference) * 2)));
        int available = energy.extractEnergy(required, true);
        if (available > 0) {
            energy.extractEnergy(available, false);
            volume.approachTemperature(targetTemperatureK, Math.min(0.05, available / 4_000.0));
            setChanged();
        }
    }

    public IEnergyStorage energyStorage() { return energy; }
    public double targetTemperatureK() { return targetTemperatureK; }
    public void setTargetTemperatureK(double value) {
        if (!Double.isFinite(value) || value < MIN_TEMPERATURE_K || value > MAX_TEMPERATURE_K)
            throw new IllegalArgumentException("Temperature must be between 1 K and 5000 K");
        targetTemperatureK = value;
        setChanged();
    }

    protected void equalizeNetwork() {
        if (level == null || level.isClientSide || !permitsFlow() || volume.amount() == 0) return;
        for (Direction direction : Direction.values()) {
            if (!(level.getBlockEntity(worldPosition.relative(direction)) instanceof ChemicalNode target)
                    || !target.permitsFlow() || target.chemicalVolume() == volume) continue;
            double pressureDifference = volume.pressureKpa() - target.chemicalVolume().pressureKpa();
            if (pressureDifference <= 0.5) continue;
            long rate = Math.max(1, Math.min(250, (long) (pressureDifference * 0.5)));
            if (volume.transferTo(target.chemicalVolume(), rate) > 0) {
                setChanged();
                if (target instanceof BlockEntity targetEntity) targetEntity.setChanged();
            }
        }
    }

    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("chemical", volume.chemical());
        tag.putLong("amount", volume.amount());
        tag.putDouble("temperature", volume.temperatureK());
        tag.putDouble("targetTemperature", targetTemperatureK);
        tag.putInt("energy", energy.getEnergyStored());
    }

    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        volume.fill(tag.getString("chemical"), Math.min(tag.getLong("amount"), volume.capacity()),
                tag.contains("temperature") ? tag.getDouble("temperature") : ChemicalVolume.AMBIENT_K);
        targetTemperatureK = tag.contains("targetTemperature") ? tag.getDouble("targetTemperature") : ChemicalVolume.AMBIENT_K;
        if (tag.contains("energy")) energy.deserializeNBT(registries, tag.get("energy"));
    }
}
