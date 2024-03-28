package com.skirlez.fabricatedexchange.emi;

import com.skirlez.fabricatedexchange.emc.EmcData;
import com.skirlez.fabricatedexchange.util.SuperNumber;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.Item;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public class FuelTransmutationEmiRecipe implements EmiRecipe {
    private final Identifier id;
    private final List<EmiIngredient> input;
    private final List<EmiStack> output;
    private final int cost;

    public FuelTransmutationEmiRecipe(Item input, Item output) {
        this.id = Identifier.of("fabricated-exchange", String.format("/transmutation/fuel/%s/%s/%s/%s", Registries.ITEM.getId(input).getNamespace(), Registries.ITEM.getId(input).getPath(), Registries.ITEM.getId(output).getNamespace(), Registries.ITEM.getId(output).getPath()));
        this.input = List.of(EmiIngredient.of(Ingredient.ofItems(input)));
        this.output = List.of(EmiStack.of(output));
        this.cost = EmcData.getItemEmc(output).toInt(0) - EmcData.getItemEmc(input).toInt(0);
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return FabricatedExchangeEmiPlugin.ENERGY_COLLECTOR_CATEGORY;
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return input;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return output;
    }

    @Override
    public int getDisplayWidth() {
        return 76;
    }

    @Override
    public int getDisplayHeight() {
        return 38;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(input.get(0), 0, 0);
        widgets.addTexture(FabricatedExchangeEmiPlugin.EMPTY_ENERGY_COLLECTOR_ARROW, 27, 0);
        widgets.addAnimatedTexture(FabricatedExchangeEmiPlugin.FULL_ENERGY_COLLECTOR_ARROW, 27, 0, 8000, true, false, false);
        widgets.addTexture(FabricatedExchangeEmiPlugin.ENERGY_COLLECTOR_SUN, 32, 8);
        widgets.addTexture(FabricatedExchangeEmiPlugin.EMPTY_ENERGY_COLLECTOR_BAR, 13, 23);
        widgets.addAnimatedTexture(FabricatedExchangeEmiPlugin.FULL_ENERGY_COLLECTOR_BAR, 15, 25, 8000, true, false, false).tooltip((mx, my) -> {
            return List.of(TooltipComponent.of(OrderedText.styledForwardsVisitedString(String.format("%s EMC", cost), Style.EMPTY)));
        });
        widgets.addSlot(output.get(0), 58, 0).recipeContext(this);
    }
}