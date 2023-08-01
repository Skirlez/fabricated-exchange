package com.skirlez.fabricatedexchange;

import java.util.function.Consumer;

import com.skirlez.fabricatedexchange.block.ModBlocks;
import com.skirlez.fabricatedexchange.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.block.Block;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.data.server.recipe.ShapelessRecipeJsonBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

// welcome to the data gen

public class FabricatedExchangeDataGenerator implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(RecipeGenerator::new);
    }
    

    private static class RecipeGenerator extends FabricRecipeProvider {
        private RecipeGenerator(FabricDataOutput generator) {
            super(generator);
        }

        @Override
        public void generate(Consumer<RecipeJsonProvider> exporter) {

            generateToAndFromBlockRecipes(ModItems.ALCHEMICAL_COAL, ModBlocks.ALCHEMICAL_COAL_BLOCK, exporter);
            generateToAndFromBlockRecipes(ModItems.RADIANT_COAL, ModBlocks.RADIANT_COAL_BLOCK, exporter);
            generateToAndFromBlockRecipes(ModItems.MOBIUS_FUEL, ModBlocks.MOBIUS_FUEL_BLOCK, exporter);
            generateToAndFromBlockRecipes(ModItems.AETERNALIS_FUEL, ModBlocks.AETERNALIS_FUEL_BLOCK, exporter);

            ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.PHILOSOPHERS_STONE)
                .pattern("RGR")
                .pattern("GDG")
                .pattern("RGR")
                .input('D', Items.DIAMOND)
                .input('R', Items.REDSTONE)
                .input('G', Items.GLOWSTONE_DUST)
                .criterion(FabricRecipeProvider.hasItem(Items.DIAMOND), FabricRecipeProvider.conditionsFromItem(Items.DIAMOND))
                .criterion(FabricRecipeProvider.hasItem(Items.REDSTONE), FabricRecipeProvider.conditionsFromItem(Items.REDSTONE))
                .offerTo(exporter);

            generatePhilosopherStoneRecipe(Items.IRON_INGOT, Items.GOLD_INGOT, 8, exporter);
            generatePhilosopherStoneRecipe(Items.GOLD_INGOT, Items.DIAMOND, 4, exporter);
            generatePhilosopherStoneRecipe(Items.DIAMOND, Items.EMERALD, 2, exporter);

            generatePhilosopherStoneRecipe(Items.COAL, ModItems.ALCHEMICAL_COAL, 4, exporter);
            generatePhilosopherStoneRecipe(ModItems.ALCHEMICAL_COAL, ModItems.RADIANT_COAL, 4, exporter);
            generatePhilosopherStoneRecipe(ModItems.RADIANT_COAL, ModItems.MOBIUS_FUEL, 4, exporter);
            generatePhilosopherStoneRecipe(ModItems.MOBIUS_FUEL, ModItems.AETERNALIS_FUEL, 4, exporter);

            TagKey<Item> coal = TagKey.of(RegistryKeys.ITEM, new Identifier("c", "coal"));
            
            ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.LOW_COVALENCE_DUST, 40)
                .input(coal)
                .input(Items.COBBLESTONE, 8)
                .criterion("has_coal", FabricRecipeProvider.conditionsFromTag(coal))
                .offerTo(exporter);

            TagKey<Item> redstone = TagKey.of(RegistryKeys.ITEM, new Identifier("c", "redstone_dusts"));
            TagKey<Item> iron = TagKey.of(RegistryKeys.ITEM, new Identifier("c", "iron_ingots"));

            ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.MEDIUM_COVALENCE_DUST, 40)
                .input(iron)
                .input(redstone)
                .criterion("has_iron", FabricRecipeProvider.conditionsFromTag(iron))
                .criterion("has_redstone", FabricRecipeProvider.conditionsFromTag(redstone))
                .offerTo(exporter);

            TagKey<Item> diamond = TagKey.of(RegistryKeys.ITEM, new Identifier("c", "diamonds"));
            
            ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.HIGH_COVALENCE_DUST, 40)
                .input(diamond)
                .input(coal)
                .input(coal)
                .input(coal)
                .input(coal)
                .criterion("has_diamond", FabricRecipeProvider.conditionsFromTag(diamond))
                .criterion("has_coal", FabricRecipeProvider.conditionsFromTag(coal))
                .offerTo(exporter);
        }

        private void generatePhilosopherStoneRecipe(Item item1, Item item2, int ratio, Consumer<RecipeJsonProvider> exporter) {
            String name1 = Registries.ITEM.getId(item1).getPath();
            String name2 = Registries.ITEM.getId(item2).getPath();
            ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, item2)
                .input(ModItems.PHILOSOPHERS_STONE)
                .input(item1, ratio)
                .criterion(FabricRecipeProvider.hasItem(ModItems.PHILOSOPHERS_STONE), 
                    FabricRecipeProvider.conditionsFromItem(ModItems.PHILOSOPHERS_STONE))
                .offerTo(exporter, "ps_" + name2 + "_from_" + name1);
                    
            ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, item1, ratio)
                .input(ModItems.PHILOSOPHERS_STONE)
                .input(item2)
                .criterion(FabricRecipeProvider.hasItem(ModItems.PHILOSOPHERS_STONE), 
                    FabricRecipeProvider.conditionsFromItem(ModItems.PHILOSOPHERS_STONE))
                .offerTo(exporter, "ps_" + name1 + "_from_" + name2);
        }
        private void generateToAndFromBlockRecipes(Item item, Block block, Consumer<RecipeJsonProvider> exporter) {
            String itemName = Registries.ITEM.getId(item).getPath();
            String blockName = Registries.ITEM.getId(block.asItem()).getPath();
            ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, item, 9)
                .input(block)
                .criterion(FabricRecipeProvider.hasItem(ModItems.PHILOSOPHERS_STONE), 
                    FabricRecipeProvider.conditionsFromItem(ModItems.PHILOSOPHERS_STONE))
                .offerTo(exporter, itemName + "_from_block");
                    
            ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, item)
                .input(item, 9)
                .criterion(FabricRecipeProvider.hasItem(ModItems.PHILOSOPHERS_STONE), 
                    FabricRecipeProvider.conditionsFromItem(ModItems.PHILOSOPHERS_STONE))
                .offerTo(exporter, blockName + "from_item");
        }


    }   
}



