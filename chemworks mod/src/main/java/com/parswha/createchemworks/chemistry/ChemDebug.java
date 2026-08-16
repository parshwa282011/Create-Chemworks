package com.parswha.createchemworks.chemistry;

import com.parswha.createchemworks.CreateChemworks;
import com.parswha.createchemworks.machinery.ChemicalNode;
import com.parswha.createchemworks.machinery.ChemicalPumpBlock;
import com.parswha.createchemworks.machinery.ChemicalPumpBlockEntity;
import com.parswha.createchemworks.machinery.ChemicalStorageBlockEntity;
import com.parswha.createchemworks.machinery.ChemicalVolume;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static net.minecraft.commands.Commands.literal;

@EventBusSubscriber(modid = CreateChemworks.MOD_ID)
public final class ChemDebug {
    private static final int MAX_NODES = 256;
    private ChemDebug() { }

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(literal("chemdebug").executes(context -> {
            openFor(context.getSource().getPlayerOrException());
            return 1;
        }));
    }

    public static void openFor(ServerPlayer player) {
        HitResult hit = player.pick(32, 0, false);
        if (!(hit instanceof BlockHitResult blockHit) || hit.getType() != HitResult.Type.BLOCK) {
            PacketDistributor.sendToPlayer(player, new OpenChemDebugPayload(List.of(
                    "No block targeted", "Look at a chemical tank, pipe, valve, or pump within 32 blocks.")));
            return;
        }
        PacketDistributor.sendToPlayer(player, new OpenChemDebugPayload(inspect(player, blockHit.getBlockPos())));
    }

    private static List<String> inspect(ServerPlayer player, BlockPos origin) {
        var level = player.serverLevel();
        List<String> lines = new ArrayList<>();
        lines.add("Dimension: " + level.dimension().location());
        lines.add("Target: " + format(origin));
        lines.add("Block: " + level.getBlockState(origin));
        BlockEntity target = level.getBlockEntity(origin);
        if (target == null) {
            lines.add("Block entity: none");
            return lines;
        }
        lines.add("Block entity: " + target.getType());
        if (target instanceof ChemicalPumpBlockEntity pump) addPump(lines, pump);
        if (!(target instanceof ChemicalNode)) {
            for (Direction direction : Direction.values()) {
                BlockPos neighbor = origin.relative(direction);
                if (level.getBlockEntity(neighbor) instanceof ChemicalNode) {
                    lines.add("");
                    lines.add("Connected chemical network:");
                    addNetwork(level, neighbor, lines);
                    return lines;
                }
            }
            lines.add("Chemical network: not connected");
            return lines;
        }
        lines.add("");
        lines.add("Connected chemical network:");
        addNetwork(level, origin, lines);
        return lines;
    }

    private static void addNetwork(net.minecraft.server.level.ServerLevel level, BlockPos start, List<String> lines) {
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        queue.add(start);
        double totalMoles = 0;
        int open = 0, closed = 0;
        List<String> nodes = new ArrayList<>();
        while (!queue.isEmpty() && visited.size() < MAX_NODES) {
            BlockPos pos = queue.removeFirst();
            if (!visited.add(pos) || !level.hasChunkAt(pos)) continue;
            BlockEntity entity = level.getBlockEntity(pos);
            if (!(entity instanceof ChemicalNode node)) continue;
            ChemicalVolume volume = node.chemicalVolume();
            totalMoles += volume.moles();
            if (node.permitsFlow()) open++; else closed++;
            nodes.add(String.format(Locale.ROOT, "%s  %s | %.3f mol | %.1f L | %.2f K | %.2f kPa | %s",
                    format(pos), volume.chemical().isBlank() ? "Empty" : volume.chemical(), volume.moles(),
                    volume.volumeLiters(), volume.temperatureK(), volume.pressureKpa(), node.permitsFlow() ? "open" : "closed"));
            for (Direction direction : Direction.values()) {
                BlockPos next = pos.relative(direction);
                if (visited.contains(next) || !level.hasChunkAt(next)) continue;
                BlockEntity neighbor = level.getBlockEntity(next);
                if (neighbor instanceof ChemicalNode) {
                    queue.add(next);
                } else if (neighbor instanceof ChemicalPumpBlockEntity pump
                        && pump.getBlockState().getValue(ChemicalPumpBlock.FACING).getAxis() == direction.getAxis()) {
                    // Pumps sit between two chemical nodes, so diagnostics follow through them.
                    BlockPos beyond = next.relative(direction);
                    if (level.hasChunkAt(beyond) && level.getBlockEntity(beyond) instanceof ChemicalNode) queue.add(beyond);
                }
            }
        }
        lines.add(String.format(Locale.ROOT, "Nodes: %d/%d | Total: %.3f mol | Open: %d | Closed: %d",
                visited.size(), MAX_NODES, totalMoles, open, closed));
        lines.addAll(nodes);
        if (!queue.isEmpty()) lines.add("WARNING: network truncated at " + MAX_NODES + " nodes");
    }

    private static void addPump(List<String> lines, ChemicalPumpBlockEntity pump) {
        Direction facing = pump.getBlockState().getValue(ChemicalPumpBlock.FACING);
        Direction output = pump.getSpeed() >= 0 ? facing : facing.getOpposite();
        lines.add(String.format(Locale.ROOT, "Pump: speed %.1f RPM | stress %s | output %s | max flow %d units/t",
                pump.getSpeed(), pump.isOverStressed() ? "OVERSTRESSED" : "OK", output,
                Math.max(1, Math.min(4_000, (long)Math.abs(pump.getSpeed()) * 4))));
    }

    private static String format(BlockPos pos) { return pos.getX() + ", " + pos.getY() + ", " + pos.getZ(); }
}
