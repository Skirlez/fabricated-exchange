package com.skirlez.fabricatedexchange.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.block.Block;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.List;

public class WorldTransmutationEmiRecipe implements EmiRecipe {
    private final Identifier id;
    private final EmiIngredient input;
    private final EmiStack output;

    public WorldTransmutationEmiRecipe(Block input, Block output) {
        this.id = Identifier.of("fabricated-exchange", String.format("/transmutation/world/%s/%s/%s/%s", Registries.BLOCK.getId(input).getNamespace(), Registries.BLOCK.getId(input).getPath(), Registries.BLOCK.getId(output).getNamespace(), Registries.BLOCK.getId(output).getPath()));
        this.input = EmiIngredient.of(Ingredient.ofItems(input));
        this.output = EmiStack.of(output);
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return FabricatedExchangeEmiPlugin.PHILOSOPHERS_STONE_CATEGORY;
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return List.of(input);
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of(output);
    }

    @Override
    public int getDisplayWidth() {
        return 125;
    }

    @Override
    public int getDisplayHeight() {
        return 18;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(input, 0, 0);
        widgets.addTexture(EmiTexture.PLUS, 27, 3);
        widgets.addSlot(FabricatedExchangeEmiPlugin.PHILOSOPHERS_STONE_ITEM, 49, 0);
        widgets.addTexture(EmiTexture.EMPTY_ARROW, 75, 1);
        widgets.addSlot(output, 107, 0).recipeContext(this);
    }
}