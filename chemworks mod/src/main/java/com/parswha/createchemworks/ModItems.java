package com.parswha.createchemworks;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import com.parswha.createchemworks.integration.tetra.MaterializedMetalItem;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(CreateChemworks.MOD_ID);

    public static final DeferredItem<Item> FLASK =
            ITEMS.registerSimpleItem("flask", new Item.Properties());

    public static final DeferredItem<Item> CREATIVE_FLASK =
            ITEMS.registerItem("creative_flask", CreativeFlaskItem::new, new Item.Properties().stacksTo(16));

    public static final DeferredItem<BlockItem> REACTION_TESTER =
            ITEMS.registerSimpleBlockItem("reaction_tester", ModBlocks.REACTION_TESTER);

    public static final DeferredItem<Item> REACTION_SCHEMATIC = ITEMS.registerItem("reaction_schematic",
            ReactionSchematicItem::new, new Item.Properties().stacksTo(1));
    public static final DeferredItem<BlockItem> REACTION_BOARD =
            ITEMS.registerSimpleBlockItem("reaction_board", ModBlocks.REACTION_BOARD);
    public static final DeferredItem<BlockItem> CHEMICAL_TANK = ITEMS.registerSimpleBlockItem("chemical_tank", ModBlocks.CHEMICAL_TANK);
    public static final DeferredItem<BlockItem> CHEMICAL_PIPE = ITEMS.registerSimpleBlockItem("chemical_pipe", ModBlocks.CHEMICAL_PIPE);
    public static final DeferredItem<BlockItem> CHEMICAL_VALVE = ITEMS.registerSimpleBlockItem("chemical_valve", ModBlocks.CHEMICAL_VALVE);
    public static final DeferredItem<BlockItem> CHEMICAL_PUMP = ITEMS.registerSimpleBlockItem("chemical_pump", ModBlocks.CHEMICAL_PUMP);
    public static final DeferredItem<BlockItem> REACTION_CONTROLLER = ITEMS.registerSimpleBlockItem("reaction_controller", ModBlocks.REACTION_CONTROLLER);
    public static final DeferredItem<Item> MATERIALIZED_METAL = ITEMS.registerItem("materialized_metal",
            MaterializedMetalItem::new, new Item.Properties());
    public static final DeferredItem<BlockItem> CHAOS_STONE = ITEMS.registerSimpleBlockItem("chaos_stone", ModBlocks.CHAOS_STONE);
    public static final DeferredItem<BlockItem> T_METAL_ORE = ITEMS.registerSimpleBlockItem("t_metal_ore", ModBlocks.T_METAL_ORE);
    public static final DeferredItem<BlockItem> PARTICLE_ACCELERATOR = ITEMS.registerSimpleBlockItem("particle_accelerator", ModBlocks.PARTICLE_ACCELERATOR);

    private ModItems() {
    }
}
