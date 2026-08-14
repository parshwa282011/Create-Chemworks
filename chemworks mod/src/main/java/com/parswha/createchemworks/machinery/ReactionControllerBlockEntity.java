package com.parswha.createchemworks.machinery;

import com.parswha.createchemworks.ModBlockEntities;
import com.parswha.createchemworks.chemistry.ReactionResultSnapshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.antarcticgardens.cna.content.heat.HeatBlockEntity;

public final class ReactionControllerBlockEntity extends ChemicalStorageBlockEntity {
    private ReactionResultSnapshot reaction;
    private double progress;

    public ReactionControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REACTION_CONTROLLER.get(), pos, state, 16_000);
    }

    public void configure(ReactionResultSnapshot reaction) { this.reaction = reaction; setChanged(); }
    public ReactionResultSnapshot reaction() { return reaction; }

    public static void tick(Level level, BlockPos pos, BlockState state, ReactionControllerBlockEntity self) {
        self.equalizeNetwork();
        if (self.reaction == null) return;
        // Create: New Age heaters consume electricity themselves. Their exposed heat is
        // the controller's energy budget, so an unpowered/cold heater cannot hold temperature.
        double availableHeat = 0;
        for (Direction direction : Direction.values()) {
            BlockEntity neighbour = level.getBlockEntity(pos.relative(direction));
            if (neighbour instanceof HeatBlockEntity heater) availableHeat = Math.max(availableHeat, heater.getHeat());
        }
        if (availableHeat > 0) {
            double factor = Math.min(0.15, availableHeat / 20_000.0);
            self.chemicalVolume().approachTemperature(self.reaction.minimumTemperatureK(), factor);
            self.setChanged();
        } else {
            self.chemicalVolume().approachTemperature(ChemicalVolume.AMBIENT_K, 0.002);
        }
        self.advanceReaction();
    }

    private void advanceReaction() {
        if (reaction == null || chemicalVolume().amount() == 0
                || chemicalVolume().temperatureK() < reaction.minimumTemperatureK()
                || chemicalVolume().pressureKpa() < reaction.pressureKpa()) return;
        String[] sides = reaction.equation().split("→|⇌", 2);
        if (sides.length != 2) return;
        String contents = chemicalVolume().chemical().replace("₂", "2").replace("₃", "3").replace("₄", "4");
        for (String token : sides[0].split("\\+")) {
            String formula = token.trim().replaceFirst("^\\d+\\s*", "");
            if (!contents.contains(formula)) return;
        }
        progress += Math.max(0.001, reaction.forwardRate());
        if (progress < 20) return;
        if (chemicalVolume().transform(chemicalVolume().chemical(), sides[1])) setChanged();
        progress = 0;
    }

    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (reaction != null) tag.put("reaction", reaction.toTag());
        tag.putDouble("progress", progress);
    }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        reaction = tag.contains("reaction") ? ReactionResultSnapshot.fromTag(tag.getCompound("reaction")) : null;
        progress = tag.getDouble("progress");
    }
}
