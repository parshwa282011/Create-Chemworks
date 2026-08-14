package com.parswha.createchemworks.machinery;

import com.parswha.createchemworks.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.core.Direction;

public class ChemicalStorageBlockEntity extends BlockEntity implements ChemicalNode {
    private final ChemicalVolume volume;

    public ChemicalStorageBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.CHEMICAL_STORAGE.get(), pos, state,
                state.getBlock() instanceof ChemicalValveBlock ? 1_000 : 16_000);
    }

    protected ChemicalStorageBlockEntity(net.minecraft.world.level.block.entity.BlockEntityType<?> type,
            BlockPos pos, BlockState state, long capacity) {
        super(type, pos, state);
        volume = new ChemicalVolume(capacity);
    }

    @Override public ChemicalVolume chemicalVolume() { return volume; }
    @Override public boolean permitsFlow() {
        return !getBlockState().hasProperty(ChemicalValveBlock.OPEN)
                || getBlockState().getValue(ChemicalValveBlock.OPEN);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ChemicalStorageBlockEntity self) {
        self.equalizeNetwork();
    }

    protected void equalizeNetwork() {
        if (level == null || level.isClientSide || !permitsFlow() || volume.amount() == 0) return;
        volume.decayPumpPressure(0.02);
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
        tag.putDouble("pumpPressure", volume.pumpPressureKpa());
    }

    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        volume.fill(tag.getString("chemical"), Math.min(tag.getLong("amount"), volume.capacity()),
                tag.contains("temperature") ? tag.getDouble("temperature") : ChemicalVolume.AMBIENT_K);
        volume.setPumpPressureKpa(tag.getDouble("pumpPressure"));
    }
}
