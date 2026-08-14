package com.parswha.createchemworks.integration.jei;

import com.parswha.createchemworks.CreateChemworks;
import com.parswha.createchemworks.ModItems;
import com.parswha.createchemworks.chemistry.ElementProfile;
import com.parswha.createchemworks.chemistry.ReactionCalculator;
import com.parswha.createchemworks.client.Elements.Element;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

final class ElementCatalogueCategory implements IRecipeCategory<Element> {
    private final IDrawable icon;

    ElementCatalogueCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemLike(ModItems.CREATIVE_FLASK.get());
    }

    @Override public RecipeType<Element> getRecipeType() { return ChemworksJeiPlugin.ELEMENT_TYPE; }
    @Override public Component getTitle() { return Component.literal("Elements in Creative Flask"); }
    @Override public IDrawable getIcon() { return icon; }
    @Override public int getWidth() { return 168; }
    @Override public int getHeight() { return 92; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Element element, IFocusGroup focuses) {
        builder.addSlot(mezz.jei.api.recipe.RecipeIngredientRole.CATALYST, 144, 4)
                .setStandardSlotBackground()
                .addItemLike(ModItems.CREATIVE_FLASK.get());
    }

    @Override
    public void draw(Element element, IRecipeSlotsView slots, GuiGraphics graphics,
                     double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        ElementProfile profile = ElementProfile.calculate(element);
        graphics.fill(4, 4, 28, 28, element.block().color());
        graphics.drawCenteredString(font, element.symbol(), 16, 12, 0xFFFFFFFF);
        graphics.drawString(font, element.number() + " — " + element.name(), 34, 5, 0xFF202020, false);
        graphics.drawString(font, "Period " + element.period() + " • "
                + element.block().label().toUpperCase() + "-block", 34, 17, 0xFF505050, false);
        graphics.drawString(font, "Mass: " + profile.atomicMass() + " u", 4, 36, 0xFF303030, false);
        graphics.drawString(font, "Phase: " + profile.standardPhase(), 4, 48, 0xFF303030, false);
        graphics.drawString(font, "Oxidation states: " + profile.oxidationStates(), 4, 60, 0xFF303030, false);
        String source = element.acceleratorOnly()
                ? "Source: Particle Accelerator" : "Source: natural deposit or processing";
        graphics.drawString(font, source, 4, 72, element.acceleratorOnly() ? 0xFFAA33AA : 0xFF306030, false);
        graphics.drawString(font, "Calculated reactions: " + ReactionCalculator.involving(element.number()).size(),
                4, 84, 0xFF505050, false);
    }

    @Override
    public ResourceLocation getRegistryName(Element element) {
        return ResourceLocation.fromNamespaceAndPath(CreateChemworks.MOD_ID, "element_" + element.number());
    }
}
