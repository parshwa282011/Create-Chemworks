package com.parswha.createchemworks.integration.jei;

import com.parswha.createchemworks.CreateChemworks;
import com.parswha.createchemworks.ModItems;
import com.parswha.createchemworks.chemistry.KnownCompound;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

final class KnownCompoundCategory implements IRecipeCategory<KnownCompound> {
    private final IDrawable icon;

    KnownCompoundCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableItemLike(ModItems.CREATIVE_FLASK.get());
    }

    @Override public RecipeType<KnownCompound> getRecipeType() { return ChemworksJeiPlugin.COMPOUND_TYPE; }
    @Override public Component getTitle() { return Component.literal("Known Real Compounds"); }
    @Override public IDrawable getIcon() { return icon; }
    @Override public int getWidth() { return 168; }
    @Override public int getHeight() { return 92; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, KnownCompound compound, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.CATALYST, 146, 5)
                .setStandardSlotBackground().addItemLike(ModItems.CREATIVE_FLASK.get());
    }

    @Override
    public void draw(KnownCompound compound, IRecipeSlotsView slots, GuiGraphics graphics,
                     double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        graphics.drawString(font, compound.name(), 4, 5, 0xFF202020, false);
        graphics.drawString(font, compound.formula() + " • " + compound.category(), 4, 18, 0xFF306080, false);
        graphics.drawString(font, "Phase: " + compound.standardPhase(), 4, 34, 0xFF404040, false);
        graphics.drawString(font, "Molar mass: " + compound.molarMass() + " g/mol", 4, 46, 0xFF404040, false);
        if (compound.isFuel()) {
            graphics.drawString(font, "Fuel energy: " + compound.energyDensityMjKg() + " MJ/kg",
                    4, 58, 0xFF9A4A10, false);
            graphics.drawString(font, "Ignition: ~" + compound.ignitionTemperatureK() + " K",
                    4, 70, 0xFF9A4A10, false);
        }
        graphics.drawString(font, compound.hazards(), 4, 82, 0xFFA02020, false);
    }

    @Override
    public ResourceLocation getRegistryName(KnownCompound compound) {
        return ResourceLocation.fromNamespaceAndPath(CreateChemworks.MOD_ID,
                "known_compound_" + compound.id());
    }
}
