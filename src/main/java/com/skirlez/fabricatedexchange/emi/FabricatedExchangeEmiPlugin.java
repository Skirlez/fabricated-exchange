package com.skirlez.fabricatedexchange.emi;

import com.skirlez.fabricatedexchange.block.ModBlocks;
import com.skirlez.fabricatedexchange.item.ModItems;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.EmiRecipeSorting;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import com.skirlez.fabricatedexchange.BlockTransmutation;
import com.skirlez.fabricatedexchange.FabricatedExchange;

import java.util.Map;

public class FabricatedExchangeEmiPlugin implements EmiPlugin {
    public static final Identifier WIDGETS = new Identifier("fabricated-exchange", "textures/gui/emi/widgets.png");
    public static final EmiTexture EMPTY_ENERGY_COLLECTOR_ARROW = new EmiTexture(WIDGETS, 0, 0, 25, 11);
    public static final EmiTexture FULL_ENERGY_COLLECTOR_ARROW = new EmiTexture(WIDGETS, 25, 0, 25, 11);
    public static final EmiTexture EMPTY_ENERGY_COLLECTOR_BAR = new EmiTexture(WIDGETS, 50, 0, 52, 14);
    public static final EmiTexture FULL_ENERGY_COLLECTOR_BAR = new EmiTexture(WIDGETS, 102, 0, 48, 10);
    public static final EmiTexture ENERGY_COLLECTOR_SUN = new EmiTexture(WIDGETS, 0, 11, 14, 14);

    public static final Identifier PHILOSOPHERS_STONE_ICON = new Identifier("fabricated-exchange", "textures/gui/emi/philosophers_stone_simplified.png");
    public static final EmiStack PHILOSOPHERS_STONE_ITEM = EmiStack.of(ModItems.PHILOSOPHERS_STONE);
    public static final EmiRecipeCategory PHILOSOPHERS_STONE_CATEGORY = new EmiRecipeCategory(new Identifier("fabricated-exchange", "philosophers_stone"), PHILOSOPHERS_STONE_ITEM, new EmiTexture(PHILOSOPHERS_STONE_ICON, 0, 0, 16, 16, 16, 16, 16, 16), EmiRecipeSorting.identifier());

    public static final Identifier ENERGY_COLLECTOR_ICON = new Identifier("fabricated-exchange", "textures/gui/emi/energy_collector_simplified.png");
    public static final EmiStack ENERGY_COLLECTOR_MK1_ITEM = EmiStack.of(ModBlocks.ENERGY_COLLECTOR_MK1);
    public static final EmiStack ENERGY_COLLECTOR_MK2_ITEM = EmiStack.of(ModBlocks.ENERGY_COLLECTOR_MK2);
    public static final EmiStack ENERGY_COLLECTOR_MK3_ITEM = EmiStack.of(ModBlocks.ENERGY_COLLECTOR_MK3);
    public static final EmiRecipeCategory ENERGY_COLLECTOR_CATEGORY = new EmiRecipeCategory(new Identifier("fabricated-exchange", "energy_collector"), ENERGY_COLLECTOR_MK1_ITEM, new EmiTexture(ENERGY_COLLECTOR_ICON, 0, 0, 16, 16, 16, 16, 16, 16), EmiRecipeSorting.identifier());

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(PHILOSOPHERS_STONE_CATEGORY);
        registry.addWorkstation(PHILOSOPHERS_STONE_CATEGORY, PHILOSOPHERS_STONE_ITEM);
        for (Map.Entry<Block, Block> entry : BlockTransmutation.blockTransmutationMap.entrySet()) {
            registry.addRecipe(new WorldTransmutationEmiRecipe(entry.getKey(), entry.getValue()));
        }

        registry.addCategory(ENERGY_COLLECTOR_CATEGORY);
        registry.addWorkstation(ENERGY_COLLECTOR_CATEGORY, ENERGY_COLLECTOR_MK1_ITEM);
        registry.addWorkstation(ENERGY_COLLECTOR_CATEGORY, ENERGY_COLLECTOR_MK2_ITEM);
        registry.addWorkstation(ENERGY_COLLECTOR_CATEGORY, ENERGY_COLLECTOR_MK3_ITEM);
        for (Map.Entry<Item, Item> entry : FabricatedExchange.fuelProgressionMap.entrySet()) {
            registry.addRecipe(new FuelTransmutationEmiRecipe(entry.getKey(), entry.getValue()));
        }
    }
}
