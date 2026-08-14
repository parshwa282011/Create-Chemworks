package com.parswha.createchemworks.chemistry;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.parswha.createchemworks.CreateChemworks;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = CreateChemworks.MOD_ID)
public final class ReactionTesterCommand {
    private ReactionTesterCommand() { }

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("reactiontest")
                .then(Commands.literal("batch")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> runBatch(context.getSource(), 1000))
                        .then(Commands.argument("count", IntegerArgumentType.integer(1, 100_000))
                                .executes(context -> runBatch(context.getSource(),
                                        IntegerArgumentType.getInteger(context, "count")))))
                .then(Commands.argument("temperatureK", DoubleArgumentType.doubleArg(0))
                .then(Commands.argument("pressureKpa", DoubleArgumentType.doubleArg(0.001))
                .then(Commands.argument("reactants", StringArgumentType.greedyString())
                .executes(context -> run(context.getSource(),
                        DoubleArgumentType.getDouble(context, "temperatureK"),
                        DoubleArgumentType.getDouble(context, "pressureKpa"),
                        StringArgumentType.getString(context, "reactants")))))));
    }

    private static int runBatch(net.minecraft.commands.CommandSourceStack source, int count) {
        long started = System.nanoTime();
        List<String> report = new ArrayList<>();
        int errors = 0;
        int viable = 0;
        var elements = com.parswha.createchemworks.client.Elements.ElementsData.all();
        report.add("Create: Chemworks reaction test");
        report.add("Requested reactions: " + count);
        report.add("Temperature: 1200 K; pressure: 101.325 kPa");
        report.add("");
        for (int index = 0; index < count; index++) {
            var left = elements.get(Math.floorMod(index * 37 + 3, elements.size()));
            var right = elements.get(Math.floorMod(index * 101 + 17, elements.size()));
            try {
                LiveReactionCalculator.Result result = LiveReactionCalculator.calculate(
                        left.symbol(), right.symbol(), 1200.0, 101.325);
                if (result.conditionsMet()) viable++;
                if (!Double.isFinite(result.product().molarMass())
                        || !Double.isFinite(result.reaction().estimatedEnthalpyKj())) {
                    throw new IllegalStateException("non-finite calculated property");
                }
            } catch (Throwable error) {
                errors++;
                report.add("ERROR #" + index + " [" + left.symbol() + " + " + right.symbol()
                        + "]: " + error.getClass().getSimpleName() + ": " + error.getMessage());
            }
        }
        long elapsedMs = (System.nanoTime() - started) / 1_000_000L;
        report.add("");
        report.add("Completed: " + count);
        report.add("Viable: " + viable);
        report.add("Errors: " + errors);
        report.add("Elapsed ms: " + elapsedMs);
        String stamp = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").format(LocalDateTime.now());
        Path path = source.getServer().getServerDirectory().resolve("logs")
                .resolve("create-chemworks-reaction-test-" + stamp + ".log");
        try {
            Files.createDirectories(path.getParent());
            Files.write(path, report, StandardCharsets.UTF_8);
        } catch (Exception error) {
            source.sendFailure(Component.literal("Tests ran, but the report could not be saved: " + error));
            return 0;
        }
        int finalErrors = errors;
        source.sendSuccess(() -> Component.literal("Ran " + count + " reactions in " + elapsedMs
                + " ms: " + finalErrors + " errors. Report: " + path).withStyle(
                finalErrors == 0 ? ChatFormatting.GREEN : ChatFormatting.YELLOW), true);
        return finalErrors == 0 ? 1 : 0;
    }

    private static int run(net.minecraft.commands.CommandSourceStack source, double temperature,
                           double pressure, String reactants) {
        String[] sides = reactants.split("\\s*\\+\\s*", 2);
        if (sides.length != 2) {
            source.sendFailure(Component.literal("Use two reactants separated by +, for example: H2 + O2"));
            return 0;
        }
        try {
            LiveReactionCalculator.Result result = LiveReactionCalculator.calculate(
                    sides[0], sides[1], temperature, pressure);
            WorldReactionData data = source.getServer().overworld().getDataStorage()
                    .computeIfAbsent(WorldReactionData.factory(), WorldReactionData.FILE_NAME);
            ChemicalReaction reaction = data.remember(result);
            CalculatedCompound product = result.product();
            source.sendSuccess(() -> Component.literal(reaction.equation()).withStyle(ChatFormatting.AQUA), false);
            source.sendSuccess(() -> Component.literal(result.conditionsMet() ? "Reaction can proceed" :
                    "Conditions insufficient: needs about " + reaction.minimumTemperatureK() + " K and "
                            + reaction.pressureKpa() + " kPa").withStyle(result.conditionsMet()
                    ? ChatFormatting.GREEN : ChatFormatting.RED), false);
            source.sendSuccess(() -> Component.literal("ΔH " + reaction.estimatedEnthalpyKj() + " kJ/mol | "
                    + product.bondType() + " | " + product.phase() + " | molar mass " + product.molarMass()), false);
            ReactionKinetics rates = ReactionKinetics.estimate(reaction, temperature, pressure, 1, 1, 0);
            source.sendSuccess(() -> Component.literal(rates.reversible()
                    ? "Reversible | forward rate " + rates.forwardRate() + " " + rates.unit()
                            + " | reverse rate " + rates.reverseRate() + " " + rates.unit()
                    : "Forward-only rate " + rates.forwardRate() + " " + rates.unit()), false);
            return result.conditionsMet() ? 1 : 0;
        } catch (IllegalArgumentException exception) {
            source.sendFailure(Component.literal(exception.getMessage()));
            return 0;
        }
    }
}
