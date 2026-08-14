package com.parswha.createchemworks.integration.jei;

import com.parswha.createchemworks.CreateChemworks;
import com.parswha.createchemworks.ModItems;
import com.parswha.createchemworks.chemistry.ChemicalReaction;
import com.parswha.createchemworks.chemistry.ReactionCalculator;
import com.parswha.createchemworks.chemistry.KnownCompound;
import com.parswha.createchemworks.chemistry.KnownCompounds;
import com.parswha.createchemworks.client.Elements.Element;
import com.parswha.createchemworks.client.Elements.ElementsData;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

@JeiPlugin
public final class ChemworksJeiPlugin implements IModPlugin {
    private List<ChemicalReaction> registeredSnapshot = List.of();
    private IJeiRuntime runtime;
    public static final RecipeType<ChemicalReaction> REACTION_TYPE = RecipeType.create(
            CreateChemworks.MOD_ID, "calculated_reaction", ChemicalReaction.class);
    public static final RecipeType<Element> ELEMENT_TYPE = RecipeType.create(
            CreateChemworks.MOD_ID, "element_catalogue", Element.class);
    public static final RecipeType<KnownCompound> COMPOUND_TYPE = RecipeType.create(
            CreateChemworks.MOD_ID, "known_compound", KnownCompound.class);

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(CreateChemworks.MOD_ID, "jei");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                new CalculatedReactionCategory(guiHelper),
                new ElementCatalogueCategory(guiHelper),
                new KnownCompoundCategory(guiHelper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registeredSnapshot = ReactionCalculator.reactions();
        registration.addRecipes(REACTION_TYPE, registeredSnapshot);
        registration.addRecipes(ELEMENT_TYPE, ElementsData.all());
        registration.addRecipes(COMPOUND_TYPE, KnownCompounds.ALL);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(ModItems.CREATIVE_FLASK.get(),
                REACTION_TYPE, ELEMENT_TYPE, COMPOUND_TYPE);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
        ReactionCalculator.onCatalogueChanged(this::replaceJeiCatalogue);
    }

    @Override
    public void onRuntimeUnavailable() {
        runtime = null;
        ReactionCalculator.onCatalogueChanged(ignored -> { });
    }

    private void replaceJeiCatalogue(List<ChemicalReaction> updated) {
        if (runtime == null || registeredSnapshot.equals(updated)) return;
        if (!registeredSnapshot.isEmpty()) {
            runtime.getRecipeManager().hideRecipes(REACTION_TYPE, registeredSnapshot);
        }
        runtime.getRecipeManager().addRecipes(REACTION_TYPE, updated);
        registeredSnapshot = updated;
    }
}
