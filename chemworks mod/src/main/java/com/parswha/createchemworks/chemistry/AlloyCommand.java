package com.parswha.createchemworks.chemistry;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.parswha.createchemworks.CreateChemworks;
import com.parswha.createchemworks.client.Elements.Element;
import com.parswha.createchemworks.client.Elements.ElementsData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import com.parswha.createchemworks.integration.tetra.ChemworksTetraIntegration;
import com.parswha.createchemworks.integration.tetra.MaterializedMetalItem;
import se.mickelus.tetra.api.material.PublishResult;

@EventBusSubscriber(modid = CreateChemworks.MOD_ID)
public final class AlloyCommand {
    private AlloyCommand() { }

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("alloycalc")
                .then(Commands.literal("named")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .then(Commands.argument("composition", StringArgumentType.greedyString())
                                        .executes(context -> calculate(context.getSource(),
                                                StringArgumentType.getString(context, "composition"),
                                                StringArgumentType.getString(context, "name"))))))
                .then(Commands.argument("composition", StringArgumentType.greedyString())
                        .executes(context -> calculate(context.getSource(),
                                StringArgumentType.getString(context, "composition"), null))));
        event.getDispatcher().register(Commands.literal("alloyproperties")
                .executes(context -> showProperties(context.getSource())));
        event.getDispatcher().register(Commands.literal("alloytestset")
                .requires(source -> source.hasPermission(2))
                .executes(context -> giveTestSet(context.getSource())));
    }

    private static int calculate(net.minecraft.commands.CommandSourceStack source, String input, String requestedName) {
        try {
            List<AlloyCalculator.Constituent> constituents = parse(input);
            AlloyProperties result = AlloyCalculator.calculate(constituents);
            String canonical = constituents.stream()
                    .sorted(Comparator.comparingInt(value -> value.element().number()))
                    .map(value -> value.element().number() + "=" + value.percent())
                    .reduce((left, right) -> left + ";" + right).orElseThrow();
            String hash = UUID.nameUUIDFromBytes(canonical.getBytes(StandardCharsets.UTF_8))
                    .toString().replace("-", "");
            ResourceLocation materialId = ResourceLocation.fromNamespaceAndPath(CreateChemworks.MOD_ID, "alloy/" + hash);
            String alloyName = requestedName == null ? alloyName(constituents) : validateName(requestedName);
            PublishResult publication = ChemworksTetraIntegration.publishAlloy(materialId, alloyName, result);
            if (publication.status() == PublishResult.Status.REJECTED) {
                throw new IllegalArgumentException("Tetra rejected this alloy: " + publication.message());
            }
            source.getServer().overworld().getDataStorage()
                    .computeIfAbsent(WorldAlloyData.factory(), WorldAlloyData.FILE_NAME)
                    .remember(materialId, alloyName, result);
            if (!(source.getEntity() instanceof net.minecraft.server.level.ServerPlayer player)) {
                throw new IllegalArgumentException("Run /alloycalc as a player to receive the alloy flasks");
            }
            // Tetra modules can require several units. A full stack makes it practical
            // to test tool heads, handles, and armor plates from one calculation.
            var flasks = MaterializedMetalItem.createFlasks(materialId, alloyName + " Flask", 64);
            if (!player.addItem(flasks)) player.drop(flasks, false);
            source.sendSuccess(() -> Component.literal("Calculated alloy: " + input)
                    .withStyle(ChatFormatting.GOLD), false);
            source.sendSuccess(() -> Component.literal("Gave 64 Tetra-ready alloy flasks (material "
                    + materialId + ")").withStyle(ChatFormatting.AQUA), false);
            source.sendSuccess(() -> Component.literal("Strength " + result.strength()
                    + " | Hardness " + result.hardness() + " | Toughness " + result.toughness()
                    + " | Attack speed " + result.attackSpeed()), false);
            source.sendSuccess(() -> Component.literal("Conductivity " + result.conductivity()
                    + " | Heat resistance " + result.heatResistance()
                    + " | Corrosion resistance " + result.corrosionResistance()), false);
            source.sendSuccess(() -> Component.literal("Density " + result.density()
                    + " | Brittleness " + result.brittleness() + " | Toxicity " + result.toxicity()
                    + " | Instability " + result.instability()
                    + " | Traits " + String.join(", ", result.positiveTraits())), false);
            return 1;
        } catch (IllegalArgumentException exception) {
            source.sendFailure(Component.literal(exception.getMessage()));
            source.sendFailure(Component.literal("Usage: /alloycalc Fe=70 Cr=18 Ni=10 C=2"));
            return 0;
        }
    }

    private static String validateName(String name) {
        String clean = name.trim();
        if (clean.isBlank() || clean.length() > 48) throw new IllegalArgumentException("Alloy name must be 1–48 characters");
        return clean;
    }

    private static int showProperties(net.minecraft.commands.CommandSourceStack source) {
        if (!(source.getEntity() instanceof net.minecraft.server.level.ServerPlayer player)) {
            source.sendFailure(Component.literal("Run this command as a player while holding an alloy flask or Tetra tool"));
            return 0;
        }
        var stack = player.getMainHandItem();
        String raw = MaterializedMetalItem.materialId(stack).map(ResourceLocation::toString).orElseGet(() ->
                stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                        net.minecraft.world.item.component.CustomData.EMPTY).copyTag().getString("TetraRuntimeMaterial"));
        ResourceLocation id = ResourceLocation.tryParse(raw);
        var material = id == null ? null : se.mickelus.tetra.api.material.TetraRuntimeMaterialRegistry.INSTANCE.get(id).orElse(null);
        if (material == null) { source.sendFailure(Component.literal("The held item has no live alloy material data")); return 0; }
        source.sendSuccess(() -> Component.literal(material.displayName == null ? id.toString() : material.displayName).withStyle(ChatFormatting.GOLD), false);
        var p = material.properties;
        source.sendSuccess(() -> Component.literal("Good — attack speed " + value(p, "attack_speed") + ", heat " + value(p, "heat_resistance") + ", corrosion " + value(p, "corrosion_resistance") + ", conductivity " + value(p, "conductivity")), false);
        source.sendSuccess(() -> Component.literal("Bad — brittleness " + value(p, "brittleness") + ", toxicity " + value(p, "toxicity") + ", instability " + value(p, "instability")), false);
        if (p.has("create_chemworks:positive_traits")) source.sendSuccess(() -> Component.literal("Traits — " + p.get("create_chemworks:positive_traits")), false);
        return 1;
    }

    private static String value(com.google.gson.JsonObject properties, String key) {
        var value = properties.get("create_chemworks:" + key);
        return value == null ? "n/a" : value.getAsString();
    }

    private static int giveTestSet(net.minecraft.commands.CommandSourceStack source) {
        if (!(source.getEntity() instanceof net.minecraft.server.level.ServerPlayer player)) {
            source.sendFailure(Component.literal("Run /alloytestset as a player"));
            return 0;
        }
        var saved = source.getServer().overworld().getDataStorage()
                .computeIfAbsent(WorldAlloyData.factory(), WorldAlloyData.FILE_NAME);
        for (int tier = 1; tier <= 7; tier++) {
            AlloyProperties properties = calibrationAlloy(tier);
            int actualTier = ChemworksTetraIntegration.alloyTier(properties);
            if (actualTier != tier) throw new IllegalStateException("Calibration alloy tier mismatch: expected " + tier + ", got " + actualTier);
            String name = "Survival Calibration Alloy T" + tier;
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(CreateChemworks.MOD_ID, "alloy/test_tier_" + tier);
            PublishResult result = ChemworksTetraIntegration.publishAlloy(id, name, properties);
            if (result.status() == PublishResult.Status.REJECTED) {
                source.sendFailure(Component.literal("Tetra rejected tier " + tier + ": " + result.message()));
                return 0;
            }
            saved.remember(id, name, properties);
            ItemStack stack = MaterializedMetalItem.createFlasks(id, name + " Flask", 16);
            if (!player.addItem(stack)) player.drop(stack, false);
        }
        source.sendSuccess(() -> Component.literal("Gave 16 flasks for each persistent alloy tier (T1–T7)")
                .withStyle(ChatFormatting.AQUA), false);
        return 7;
    }

    static AlloyProperties calibrationAlloy(int tier) {
        if (tier < 1 || tier > 7) throw new IllegalArgumentException("Tier must be 1–7");
        // This profile deliberately balances one strong benefit against one drawback.
        // The scoring math lands at tier - 0.5 before the base tier is added.
        double strength = 140 * (tier - 0.975);
        double brittleness = tier % 3 == 1 ? 10.5 : 0;
        double toxicity = tier % 3 == 2 ? 9 : 0;
        double instability = tier % 3 == 0 ? 7.5 : 0;
        return new AlloyProperties(Math.max(1, strength), 12, 18, 0.5,
                20 + tier * 8, 90, 9, 8 + tier,
                brittleness, toxicity, instability, List.of("Tier " + tier + " survival calibration buff"));
    }

    private static List<AlloyCalculator.Constituent> parse(String input) {
        List<AlloyCalculator.Constituent> values = new ArrayList<>();
        for (String token : input.trim().split("[ ,]+")) {
            String[] parts = token.split("[=:]", 2);
            if (parts.length != 2) throw new IllegalArgumentException("Invalid constituent: " + token);
            Element element = ElementsData.all().stream()
                    .filter(candidate -> candidate.symbol().equalsIgnoreCase(parts[0])
                            || candidate.name().equalsIgnoreCase(parts[0]))
                    .findFirst().orElseThrow(() -> new IllegalArgumentException("Unknown element: " + parts[0]));
            double percent;
            try {
                percent = Double.parseDouble(parts[1].replace("%", ""));
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("Invalid percentage: " + parts[1]);
            }
            values.add(new AlloyCalculator.Constituent(element, percent));
        }
        return values;
    }

    static String alloyName(List<AlloyCalculator.Constituent> constituents) {
        long tCount = constituents.stream().filter(value ->
                value.element().block() == com.parswha.createchemworks.client.Elements.ElectronBlock.T).count();
        if (tCount == 36 && constituents.size() == 36) return "Omnital T-36 Superalloy";
        String base = constituents.stream()
                .sorted(Comparator.comparingDouble(AlloyCalculator.Constituent::percent).reversed()
                        .thenComparingInt(value -> value.element().number()))
                .limit(4).map(value -> value.element().symbol()).reduce((a, b) -> a + "-" + b).orElse("Custom");
        return base + (tCount == constituents.size() ? " T-Alloy" : " Alloy");
    }
}
