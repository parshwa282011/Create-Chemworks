package com.parswha.createchemworks;

import com.parswha.createchemworks.chemistry.ChemistryNetwork;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import com.parswha.createchemworks.integration.tetra.ChemworksTetraIntegration;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@Mod(CreateChemworks.MOD_ID)
public final class CreateChemworks {
    public static final String MOD_ID = "create_chemworks";

    public CreateChemworks(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.TABS.register(modEventBus);
        modEventBus.addListener(this::addCreativeTabItems);
        modEventBus.addListener(ChemistryNetwork::register);
        modEventBus.addListener(this::registerCapabilities);
        ChemworksTetraIntegration.registerApi();
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.CHEMICAL_STORAGE.get(),
                (storage, side) -> storage.energyStorage());
    }

    private void addCreativeTabItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.FLASK);
            event.accept(ModItems.CREATIVE_FLASK);
            event.accept(ModItems.CHEMIST_GOGGLES);
            event.accept(ModItems.REACTION_TESTER);
            event.accept(ModItems.REACTION_SCHEMATIC);
            event.accept(ModItems.REACTION_BOARD);
            event.accept(ModItems.CHEMICAL_TANK);
            event.accept(ModItems.CHEMICAL_PIPE);
            event.accept(ModItems.CHEMICAL_VALVE);
            event.accept(ModItems.CHEMICAL_PUMP);
            event.accept(ModItems.REACTION_CONTROLLER);
            event.accept(ModItems.CHAOS_STONE);
            event.accept(ModItems.T_METAL_ORE);
            event.accept(ModItems.PARTICLE_ACCELERATOR);
        }
    }
}
