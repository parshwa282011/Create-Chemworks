package com.parswha.createchemworks.integration.tetra;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.parswha.createchemworks.CreateChemworks;
import com.parswha.createchemworks.chemistry.AlloyProperties;
import com.parswha.createchemworks.chemistry.ChemistryRules;
import com.parswha.createchemworks.chemistry.ElementProfile;
import com.parswha.createchemworks.chemistry.TOrbitalTraits;
import com.parswha.createchemworks.client.Elements.ElectronBlock;
import com.parswha.createchemworks.client.Elements.Element;
import com.parswha.createchemworks.client.Elements.ElementsData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import se.mickelus.tetra.api.material.*;

import java.util.*;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

@EventBusSubscriber(modid = CreateChemworks.MOD_ID)
public final class ChemworksTetraIntegration {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final ResourceLocation PROVIDER_ID = id("generated_metals");
    public static final MaterialPropertyType<Double> HEAT_RESISTANCE = numeric("heat_resistance");
    public static final MaterialPropertyType<Double> CORROSION_RESISTANCE = numeric("corrosion_resistance");
    public static final MaterialPropertyType<Double> CONDUCTIVITY = numeric("conductivity");
    public static final MaterialPropertyType<Double> ATTACK_SPEED = numeric("attack_speed");
    public static final MaterialPropertyType<Double> BRITTLENESS = numeric("brittleness");
    public static final MaterialPropertyType<Double> TOXICITY = numeric("toxicity");
    public static final MaterialPropertyType<Double> INSTABILITY = numeric("instability");
    public static final MaterialPropertyType<List<String>> POSITIVE_TRAITS = new MaterialPropertyType<>() {
        private final ResourceLocation id = ChemworksTetraIntegration.id("positive_traits");
        private final Codec<List<String>> codec = Codec.STRING.listOf();
        @Override public ResourceLocation id() { return id; }
        @Override public Codec<List<String>> codec() { return codec; }
        @Override public StreamCodec<RegistryFriendlyByteBuf, List<String>> streamCodec() {
            return ByteBufCodecs.fromCodecWithRegistries(codec);
        }
        @Override public List<String> merge(List<WeightedValue<List<String>>> values, MaterialMergeContext context) {
            LinkedHashSet<String> result = new LinkedHashSet<>();
            values.forEach(value -> result.addAll(value.value()));
            return List.copyOf(result);
        }
    };
    private static MaterialProviderHandle provider;
    private static boolean registered;

    private ChemworksTetraIntegration() { }

    public static synchronized void registerApi() {
        if (registered) return;
        registered = true;
        MaterialPropertyRegistry.register(HEAT_RESISTANCE);
        MaterialPropertyRegistry.register(CORROSION_RESISTANCE);
        MaterialPropertyRegistry.register(CONDUCTIVITY);
        MaterialPropertyRegistry.register(ATTACK_SPEED);
        MaterialPropertyRegistry.register(BRITTLENESS);
        MaterialPropertyRegistry.register(TOXICITY);
        MaterialPropertyRegistry.register(INSTABILITY);
        MaterialPropertyRegistry.register(POSITIVE_TRAITS);
        TetraRuntimeMaterialRegistry.INSTANCE.registerResolver(id("materialized_metal"), 1_000,
                (stack, context) -> MaterializedMetalItem.materialId(stack));
    }

    @SubscribeEvent
    public static void serverStarted(ServerStartedEvent event) {
        registerApi();
        provider = TetraRuntimeMaterialRegistry.INSTANCE.registerProvider(PROVIDER_ID);
        List<RuntimeMaterialDefinition> definitions = ElementsData.all().stream()
                .filter(ChemistryRules::isMetal).map(ChemworksTetraIntegration::elementDefinition).toList();
        PublishResult result = provider.replaceAll(definitions);
        if (result.status() == PublishResult.Status.REJECTED) {
            throw new IllegalStateException("Tetra rejected Chemworks metals: " + result.message());
        }
        long available = definitions.stream().filter(definition -> TetraRuntimeMaterialRegistry.INSTANCE.get(definition.id()).isPresent()).count();
        if (available != definitions.size()) throw new IllegalStateException("Only " + available + "/" + definitions.size() + " Tetra metals became live");
        if (!definitions.isEmpty()) {
            var first = definitions.getFirst();
            var probe = MaterializedMetalItem.createFlasks(first.id(), "integration probe flask", 1);
            var resolution = TetraRuntimeMaterialRegistry.INSTANCE.resolve(probe, MaterialUseContext.GENERIC);
            if (resolution.status() != MaterialResolution.Status.RESOLVED || !resolution.materialId().orElseThrow().equals(first.id())) {
                throw new IllegalStateException("Tetra live material resolver failed its startup probe: " + resolution);
            }
        }
        LOGGER.info("Published and resolved {} live Chemworks metal materials in Tetra at revision {}",
                available, TetraRuntimeMaterialRegistry.INSTANCE.revision());
    }

    public static synchronized PublishResult publishAlloy(ResourceLocation id, String displayName, AlloyProperties alloy) {
        if (provider == null) throw new IllegalStateException("Tetra material provider is not available before server start");
        return provider.upsert(new RuntimeMaterialDefinition(id, PROVIDER_ID, 1, alloyJson(id, displayName, alloy)));
    }

    private static RuntimeMaterialDefinition elementDefinition(Element element) {
        ElementProfile p = ElementProfile.calculate(element);
        JsonObject json = baseJson(element.symbol().toLowerCase(Locale.ROOT), "create_chemworks:materialized_metal",
                Math.max(1, p.meltingPointK() / 80), Math.max(20, element.number() * 6),
                element.block() == ElectronBlock.T ? 8 : 1 + element.period() / 2);
        JsonObject properties = new JsonObject();
        json.addProperty("displayName", element.name());
        properties.addProperty(HEAT_RESISTANCE.id().toString(), p.meltingPointK());
        properties.addProperty(CORROSION_RESISTANCE.id().toString(), p.electronegativity() * 10);
        properties.addProperty(CONDUCTIVITY.id().toString(), 100 / (1 + p.electronegativity()));
        if (element.block() == ElectronBlock.T) {
            JsonArray traits = new JsonArray(); traits.add(TOrbitalTraits.trait(element));
            properties.add(POSITIVE_TRAITS.id().toString(), traits);
        }
        json.add("properties", properties);
        ResourceLocation id = id("element/" + element.number());
        return new RuntimeMaterialDefinition(id, PROVIDER_ID, 1, json);
    }

    private static JsonObject alloyJson(ResourceLocation id, String displayName, AlloyProperties a) {
        double tetraPrimary = Math.max(1, Math.min(12, a.strength() / 20));
        double tetraSecondary = Math.max(0.5, Math.min(4.5, 5.0 - a.attackSpeed()));
        JsonObject json = baseJson(id.getPath().replace('/', '_'), "create_chemworks:flask", tetraPrimary,
                a.toughness() * 8, Math.max(1, (int) (a.hardness() / 40)));
        json.addProperty("secondary", tetraSecondary);
        json.addProperty("displayName", displayName);
        JsonObject properties = new JsonObject();
        properties.addProperty(HEAT_RESISTANCE.id().toString(), a.heatResistance());
        properties.addProperty(CORROSION_RESISTANCE.id().toString(), a.corrosionResistance());
        properties.addProperty(CONDUCTIVITY.id().toString(), a.conductivity());
        properties.addProperty(ATTACK_SPEED.id().toString(), a.attackSpeed());
        properties.addProperty(BRITTLENESS.id().toString(), a.brittleness());
        properties.addProperty(TOXICITY.id().toString(), a.toxicity());
        properties.addProperty(INSTABILITY.id().toString(), a.instability());
        JsonArray traits = new JsonArray(); a.positiveTraits().forEach(traits::add);
        properties.add(POSITIVE_TRAITS.id().toString(), traits); json.add("properties", properties);
        return json;
    }

    private static JsonObject baseJson(String key, String carrierItem, double primary, double durability, int toolLevel) {
        JsonObject json = new JsonObject();
        json.addProperty("key", key); json.addProperty("category", "metal");
        json.addProperty("primary", Math.max(1, primary)); json.addProperty("secondary", Math.max(1, primary * 0.65));
        json.addProperty("tertiary", Math.max(1, primary * 0.5)); json.addProperty("durability", Math.max(1, durability));
        json.addProperty("integrityGain", Math.max(1, primary / 2)); json.addProperty("integrityCost", 1);
        json.addProperty("toolLevel", Math.min(4, toolLevel)); json.addProperty("toolEfficiency", Math.max(1, primary));
        JsonArray textures = new JsonArray(); textures.add("metal"); json.add("textures", textures);
        int rgb = 0x404040 | (key.hashCode() & 0xBFBFBF);
        String argb = String.format("ff%06x", rgb & 0xFFFFFF);
        JsonObject tints = new JsonObject();
        tints.addProperty("texture", argb);
        tints.addProperty("glyph", argb);
        json.add("tints", tints);
        json.addProperty("tintOverrides", true);
        JsonObject material = new JsonObject(); material.addProperty("item", carrierItem);
        json.add("material", material);
        return json;
    }

    private static MaterialPropertyType<Double> numeric(String path) {
        return new MaterialPropertyType<>() {
            private final ResourceLocation id = ChemworksTetraIntegration.id(path);
            @Override public ResourceLocation id() { return id; }
            @Override public Codec<Double> codec() { return Codec.DOUBLE; }
            @Override public StreamCodec<RegistryFriendlyByteBuf, Double> streamCodec() { return ByteBufCodecs.DOUBLE.cast(); }
            @Override public Double merge(List<WeightedValue<Double>> values, MaterialMergeContext context) {
                double total = values.stream().mapToDouble(v -> v.value() * v.weight()).sum();
                double weights = values.stream().mapToDouble(WeightedValue::weight).sum();
                return weights == 0 ? 0 : total / weights;
            }
        };
    }
    private static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath(CreateChemworks.MOD_ID, path); }
}
