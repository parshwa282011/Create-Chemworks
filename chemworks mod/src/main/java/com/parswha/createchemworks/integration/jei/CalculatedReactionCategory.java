package com.parswha.createchemworks.integration.jei;

import com.parswha.createchemworks.ModItems;
import com.parswha.createchemworks.chemistry.ChemicalReaction;
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

final class CalculatedReactionCategory implements IRecipeCategory<ChemicalReaction> {
    private final IDrawable icon;

    CalculatedReactionCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemLike(ModItems.CREATIVE_FLASK.get());
    }

    @Override public RecipeType<ChemicalReaction> getRecipeType() { return ChemworksJeiPlugin.REACTION_TYPE; }
    @Override public Component getTitle() { return Component.literal("Calculated Chemical Reaction"); }
    @Override public IDrawable getIcon() { return icon; }
    @Override public int getWidth() { return 168; }
    @Override public int getHeight() { return 72; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ChemicalReaction recipe, IFocusGroup focuses) {
        builder.addSlot(mezz.jei.api.recipe.RecipeIngredientRole.CATALYST, 146, 6)
                .setStandardSlotBackground()
                .addItemLike(ModItems.CREATIVE_FLASK.get());
    }

    @Override
    public void draw(ChemicalReaction recipe, IRecipeSlotsView slots, GuiGraphics graphics,
                     double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        graphics.drawCenteredString(font, recipe.equation(), getWidth() / 2, 1, 0xFF55FFFF);
        graphics.drawString(font, "Minimum temperature: " + Math.round(recipe.minimumTemperatureK()) + " K",
                4, 28, 0xFF404040, false);
        graphics.drawString(font, "Pressure: " + recipe.pressureKpa() + " kPa", 4, 40, 0xFF404040, false);
        graphics.drawString(font, "Estimated ΔH: " + recipe.estimatedEnthalpyKj() + " kJ", 4, 52, 0xFF404040, false);
        graphics.drawString(font, recipe.confidence(), 4, 64, 0xFF707070, false);
    }

    @Override public ResourceLocation getRegistryName(ChemicalReaction recipe) { return recipe.id(); }
}
