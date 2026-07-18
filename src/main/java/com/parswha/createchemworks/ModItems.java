package com.parswha.createchemworks;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(CreateChemworks.MOD_ID);

    public static final DeferredItem<Item> FLASK =
            ITEMS.registerSimpleItem("flask", new Item.Properties());

    public static final DeferredItem<Item> CREATIVE_FLASK =
            ITEMS.registerSimpleItem("creative_flask", new Item.Properties());

    private ModItems() {
    }
}
