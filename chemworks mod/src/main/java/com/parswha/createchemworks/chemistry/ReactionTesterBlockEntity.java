package com.parswha.createchemworks.chemistry;

import com.mojang.logging.LogUtils;
import com.parswha.createchemworks.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class ReactionTesterBlockEntity extends BlockEntity {
    public static final int CAPACITY = 9;
    private static final Logger LOGGER = LogUtils.getLogger();
    private NonNullList<ItemStack> flasks = NonNullList.withSize(CAPACITY, ItemStack.EMPTY);
    private final double[] concentrations = new double[CAPACITY];
    private final boolean[] catalysts = new boolean[CAPACITY];
    private double temperatureK = 298.15;
    private double pressureKpa = 101.325;
    private List<ReactionResultSnapshot> lastResults = List.of();
    public ReactionTesterBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.REACTION_TESTER.get(), pos, state); }

    public boolean add(ItemStack source, boolean copyOnly) {
        for (int slot = 0; slot < flasks.size(); slot++) if (flasks.get(slot).isEmpty()) {
            flasks.set(slot, source.copyWithCount(1));
            concentrations[slot] = 1.0; catalysts[slot] = false;
            if (!copyOnly) source.shrink(1);
            setChanged(); return true;
        }
        return false;
    }
    public ItemStack removeLast() {
        for (int slot = flasks.size() - 1; slot >= 0; slot--) if (!flasks.get(slot).isEmpty()) {
            ItemStack result = flasks.get(slot); flasks.set(slot, ItemStack.EMPTY); concentrations[slot]=0; catalysts[slot]=false; setChanged(); return result;
        }
        return ItemStack.EMPTY;
    }
    public List<ItemStack> flasks() { return flasks.stream().filter(s -> !s.isEmpty()).map(ItemStack::copy).toList(); }
    public ItemStack flask(int slot) { return flasks.get(slot).copy(); }
    public double temperatureK() { return temperatureK; }
    public double pressureKpa() { return pressureKpa; }
    public double concentration(int slot) { return concentrations[slot]; }
    public boolean catalyst(int slot) { return catalysts[slot]; }
    public List<ReactionResultSnapshot> lastResults() { return lastResults; }
    public void configure(double temperatureK, double pressureKpa, double[] values, boolean[] roles) {
        if (!Double.isFinite(temperatureK) || temperatureK <= 0 || temperatureK > 100_000) throw new IllegalArgumentException("Temperature must be 0–100000 K");
        if (!Double.isFinite(pressureKpa) || pressureKpa <= 0 || pressureKpa > 1.0e9) throw new IllegalArgumentException("Pressure must be positive");
        this.temperatureK=temperatureK; this.pressureKpa=pressureKpa;
        for(int i=0;i<CAPACITY;i++){ concentrations[i]=Math.max(0,Math.min(1.0e9,values[i])); catalysts[i]=roles[i]; }
        setChanged();
    }

    public Result runAll(ServerLevel level) {
        int attempted = 0, errors = 0, catalystCount = 0;
        for(boolean catalyst:catalysts) if(catalyst) catalystCount++;
        List<String> lines = new ArrayList<>();
        lines.add(LocalDateTime.now() + " tester at " + worldPosition + " | T="+temperatureK+" K | P="+pressureKpa+" kPa");
        for(int slot=0;slot<CAPACITY;slot++) if(!flasks.get(slot).isEmpty()) lines.add("INPUT "+(slot+1)+" | "+flasks.get(slot).getHoverName().getString()+" | concentration="+concentrations[slot]+" mol/L | role="+(catalysts[slot]?"catalyst":"reactant"));
        List<ReactionCandidateGenerator.Input> reactants = new ArrayList<>();
        for (int slot = 0; slot < CAPACITY; slot++) {
            if (flasks.get(slot).isEmpty() || catalysts[slot]) continue;
            String name = flasks.get(slot).getHoverName().getString();
            try {
                for (var component : FlaskContents.parse(name).components()) {
                    reactants.add(new ReactionCandidateGenerator.Input(component.formula(),
                            concentrations[slot] * component.amount()));
                }
            } catch (Throwable error) {
                errors++;
                lines.add("ERROR input " + name + " | " + error.getClass().getSimpleName() + ": " + error.getMessage());
            }
        }
        if (reactants.size() >= 2 && reactants.size() <= CAPACITY) {
            try {
                var candidates = ReactionCandidateGenerator.generate(reactants, temperatureK, pressureKpa, catalystCount);
                lastResults = candidates.stream().map(ReactionResultSnapshot::from).toList();
                attempted = candidates.size();
                for (var candidate : candidates) {
                    var result = candidate.result();
                    var rates = candidate.kinetics();
                    lines.add("OK inputs=" + candidate.inputs().size() + " | " + result.reaction().equation()
                            + " | viable=" + result.conditionsMet() + " | forward=" + rates.forwardRate() + " " + rates.unit()
                            + (rates.reversible() ? " | reversible=true | reverse=" + rates.reverseRate() + " " + rates.unit()
                            : " | forward-only"));
                }
            } catch (Throwable error) {
                errors++;
                lines.add("ERROR enumeration | " + error.getClass().getSimpleName() + ": " + error.getMessage());
            }
        } else if (reactants.size() > CAPACITY) {
            errors++;
            lines.add("ERROR enumeration | Solutions expanded beyond the nine-reactant limit");
        }
        if (reactants.size() < 2) lastResults = List.of();
        lines.add("Result: attempted=" + attempted + ", errors=" + errors);
        try {
            var path = level.getServer().getServerDirectory().resolve("logs").resolve("create-chemworks-reaction-tester.log");
            Files.createDirectories(path.getParent());
            Files.write(path, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Exception error) { LOGGER.error("Could not write reaction tester report", error); }
        LOGGER.info("Reaction tester at {} ran {} pairs with {} errors", worldPosition, attempted, errors);
        return new Result(attempted, errors);
    }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) { super.saveAdditional(tag, registries); ContainerHelper.saveAllItems(tag, flasks, registries); tag.putDouble("TemperatureK",temperatureK); tag.putDouble("PressureKpa",pressureKpa); for(int i=0;i<CAPACITY;i++){tag.putDouble("Concentration"+i,concentrations[i]);tag.putBoolean("Catalyst"+i,catalysts[i]);} }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) { super.loadAdditional(tag, registries); flasks = NonNullList.withSize(CAPACITY, ItemStack.EMPTY); ContainerHelper.loadAllItems(tag, flasks, registries); temperatureK=tag.contains("TemperatureK")?tag.getDouble("TemperatureK"):298.15; pressureKpa=tag.contains("PressureKpa")?tag.getDouble("PressureKpa"):101.325; for(int i=0;i<CAPACITY;i++){concentrations[i]=tag.contains("Concentration"+i)?tag.getDouble("Concentration"+i):1;catalysts[i]=tag.getBoolean("Catalyst"+i);} }
    public record Result(int attempted, int errors) { }
}
