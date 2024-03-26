package com.skirlez.fabricatedexchange.emi;

import com.skirlez.fabricatedexchange.item.ModItems;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import com.skirlez.fabricatedexchange.BlockTransmutation;

import java.util.Map;

public class FabricatedExchangeEmiPlugin implements EmiPlugin {
    public static final Identifier PHILOSOPHERS_STONE_ICON = new Identifier("fabricated-exchange", "textures/philosophers_stone.png");
    public static final EmiStack PHILOSOPHERS_STONE_ITEM = EmiStack.of(ModItems.PHILOSOPHERS_STONE);
    public static final EmiRecipeCategory PHILOSOPHERS_STONE_CATEGORY = new EmiRecipeCategory(new Identifier("fabricated-exchange", "philosophers_stone"), PHILOSOPHERS_STONE_ITEM, new EmiTexture(PHILOSOPHERS_STONE_ICON, 0, 0, 16, 16));
    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(PHILOSOPHERS_STONE_CATEGORY);
        registry.addWorkstation(PHILOSOPHERS_STONE_CATEGORY, PHILOSOPHERS_STONE_ITEM);
        for (Map.Entry<Block, Block> entry : BlockTransmutation.blockTransmutationMap.entrySet()) {
            registry.addRecipe(new TransmutationEmiRecipe(entry.getKey(), entry.getValue()));
        }
    }
}
