package com.parswha.createchemworks.chemistry;

import com.parswha.createchemworks.CreateChemworks;
import com.parswha.createchemworks.client.Elements.ElectronBlock;
import com.parswha.createchemworks.client.Elements.Element;
import com.parswha.createchemworks.client.Elements.ElementsData;
import com.parswha.createchemworks.integration.tetra.MaterializedMetalItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

import java.util.List;

/** Gameplay acquisition follows orbital themes while retaining deterministic material identities. */
@EventBusSubscriber(modid = CreateChemworks.MOD_ID)
public final class ElementAcquisitionEvents {
    private static final List<Element> S = block(ElectronBlock.S);
    private static final List<Element> P = block(ElectronBlock.P);
    private static final List<Element> D = block(ElectronBlock.D);
    private static final List<Element> F = block(ElectronBlock.F);
    private ElementAcquisitionEvents() { }

    @SubscribeEvent
    public static void blockDrops(BlockDropsEvent event) {
        var random = event.getLevel().random;
        Element selected = null;
        if ((event.getState().is(BlockTags.DIRT) || event.getState().is(BlockTags.BASE_STONE_OVERWORLD))
                && random.nextInt(18) == 0) selected = pick(S, random.nextInt(S.size()));
        if ((event.getState().is(BlockTags.LEAVES) || event.getState().is(BlockTags.CROPS))
                && random.nextInt(10) == 0) selected = pick(P, random.nextInt(P.size()));
        if (event.getPos().getY() < -64 && event.getState().is(BlockTags.BASE_STONE_OVERWORLD)) {
            if (random.nextInt(90) == 0) selected = pick(D, random.nextInt(D.size()));
            if (random.nextInt(320) == 0) selected = pick(F, random.nextInt(F.size()));
        }
        if (selected != null) addDrop(event, selected);
    }

    @SubscribeEvent
    public static void livingDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Animal) || event.getEntity().getRandom().nextInt(8) != 0) return;
        Element element = pick(P, event.getEntity().getRandom().nextInt(P.size()));
        var stack = stack(element);
        event.getDrops().add(new ItemEntity(event.getEntity().level(), event.getEntity().getX(),
                event.getEntity().getY(), event.getEntity().getZ(), stack));
    }

    private static void addDrop(BlockDropsEvent event, Element element) {
        event.getDrops().add(new ItemEntity(event.getLevel(), event.getPos().getX() + .5,
                event.getPos().getY() + .5, event.getPos().getZ() + .5, stack(element)));
    }
    private static net.minecraft.world.item.ItemStack stack(Element element) {
        return MaterializedMetalItem.create(ResourceLocation.fromNamespaceAndPath(CreateChemworks.MOD_ID,
                "element/" + element.number()), element.name() + " sample");
    }
    private static List<Element> block(ElectronBlock block) {
        return ElementsData.all().stream().filter(e -> e.block() == block).toList();
    }
    private static Element pick(List<Element> values, int index) { return values.get(index); }
}
