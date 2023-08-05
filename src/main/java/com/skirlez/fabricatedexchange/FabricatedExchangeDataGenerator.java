package com.skirlez.fabricatedexchange;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.skirlez.fabricatedexchange.block.ModBlocks;
import com.skirlez.fabricatedexchange.item.ModItems;
import com.skirlez.fabricatedexchange.util.ModTags;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.BlockStateVariant;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.Models;
import net.minecraft.data.client.TextureKey;
import net.minecraft.data.client.TextureMap;
import net.minecraft.data.client.VariantSettings;
import net.minecraft.data.client.VariantsBlockStateSupplier;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.data.server.recipe.ShapelessRecipeJsonBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;


// welcome to the data gen

// TODO: This class could use some organization into multiple files

public class FabricatedExchangeDataGenerator implements DataGeneratorEntrypoint {
    
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(ItemTagGenerator::new);
        pack.addProvider(BlockTagGenerator::new);
        pack.addProvider(RecipeGenerator::new);
        pack.addProvider(ModelGenerator::new);
        pack.addProvider(BlockLootTables::new);
    }

    private static class ItemTagGenerator extends FabricTagProvider.ItemTagProvider {
        public ItemTagGenerator(FabricDataOutput output, CompletableFuture<WrapperLookup> completableFuture) {
            super(output, completableFuture);
        }

        @Override
        protected void configure(WrapperLookup arg) {
            addItemsToTag(ModTags.FUEL, Items.COAL, Items.CHARCOAL, Items.REDSTONE, Items.REDSTONE_BLOCK,
                Items.GUNPOWDER, Items.COAL_BLOCK, Items.BLAZE_POWDER, Items.GLOWSTONE_DUST, Items.GLOWSTONE,
                ModItems.ALCHEMICAL_COAL, ModBlocks.ALCHEMICAL_COAL_BLOCK, ModItems.RADIANT_COAL, ModBlocks.RADIANT_COAL_BLOCK,
                ModItems.MOBIUS_FUEL, ModBlocks.MOBIUS_FUEL_BLOCK, ModItems.AETERNALIS_FUEL, ModBlocks.AETERNALIS_FUEL_BLOCK);
        }

        private void addItemsToTag(TagKey<Item> tag, ItemConvertible... items) {
            FabricTagBuilder builder = getOrCreateTagBuilder(tag);
            for (int i = 0; i < items.length; i++) {
                builder.add(items[i].asItem());
            }
        }
    }

    private static class BlockTagGenerator extends FabricTagProvider.BlockTagProvider {
        public BlockTagGenerator(FabricDataOutput output, CompletableFuture<WrapperLookup> completableFuture) {
            super(output, completableFuture);
        }

        @Override
        protected void configure(WrapperLookup arg) {
            Block[] blocks = {ModBlocks.ALCHEMICAL_COAL_BLOCK, ModBlocks.RADIANT_COAL_BLOCK, ModBlocks.MOBIUS_FUEL_BLOCK,
                ModBlocks.AETERNALIS_FUEL_BLOCK, ModBlocks.DARK_MATTER_BLOCK, ModBlocks.RED_MATTER_BLOCK,
                ModBlocks.ENERGY_COLLECTOR_MK1, ModBlocks.ENERGY_COLLECTOR_MK2, ModBlocks.ENERGY_COLLECTOR_MK3,
                ModBlocks.ANTIMATTER_RELAY_MK1, ModBlocks.ANTIMATTER_RELAY_MK2, ModBlocks.ANTIMATTER_RELAY_MK3,
                ModBlocks.ALCHEMICAL_CHEST, ModBlocks.ENERGY_CONDENSER_MK1, ModBlocks.ENERGY_CONDENSER_MK2};

            addBlocksToTag(BlockTags.PICKAXE_MINEABLE, blocks);
            addBlocksToTag(BlockTags.NEEDS_IRON_TOOL, blocks);
        }

        private void addBlocksToTag(TagKey<Block> tag, Block... blocks) {
            FabricTagBuilder builder = getOrCreateTagBuilder(tag);
            for (int i = 0; i < blocks.length; i++) {
                builder.add(blocks[i]);
            }
        }
    }

    private static class BlockLootTables extends FabricBlockLootTableProvider {
        public BlockLootTables(FabricDataOutput dataOutput) {
            super(dataOutput);
        }
    
        @Override
        public void generate() {
            dropBlocksAsThemselves(
                ModBlocks.ALCHEMICAL_COAL_BLOCK, ModBlocks.RADIANT_COAL_BLOCK, ModBlocks.MOBIUS_FUEL_BLOCK,
                ModBlocks.AETERNALIS_FUEL_BLOCK, ModBlocks.DARK_MATTER_BLOCK, ModBlocks.RED_MATTER_BLOCK,
                ModBlocks.ENERGY_COLLECTOR_MK1, ModBlocks.ENERGY_COLLECTOR_MK2, ModBlocks.ENERGY_COLLECTOR_MK3,
                ModBlocks.ANTIMATTER_RELAY_MK1, ModBlocks.ANTIMATTER_RELAY_MK2, ModBlocks.ANTIMATTER_RELAY_MK3,
                ModBlocks.ALCHEMICAL_CHEST, ModBlocks.ENERGY_CONDENSER_MK1, ModBlocks.ENERGY_CONDENSER_MK2, 
                ModBlocks.TRANSMUTATION_TABLE);
            
        }

        private void dropBlocksAsThemselves(Block... blocks) {
            for (int i = 0; i < blocks.length; i++)
                addDrop(blocks[i]);
        }
    }

    private static class ModelGenerator extends FabricModelProvider {
        private ModelGenerator(FabricDataOutput generator) {
            super(generator);
        }
    
        @Override
        public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
            registerCubeAllBlockModels(blockStateModelGenerator, 
            ModBlocks.ALCHEMICAL_COAL_BLOCK, ModBlocks.RADIANT_COAL_BLOCK, ModBlocks.MOBIUS_FUEL_BLOCK,
            ModBlocks.AETERNALIS_FUEL_BLOCK, ModBlocks.DARK_MATTER_BLOCK, ModBlocks.RED_MATTER_BLOCK);
            
            registerLeveledHorizontalOrientables(blockStateModelGenerator, "antimatter_relays", 
                ModBlocks.ANTIMATTER_RELAY_MK1, ModBlocks.ANTIMATTER_RELAY_MK2, ModBlocks.ANTIMATTER_RELAY_MK3);
            registerLeveledHorizontalOrientables(blockStateModelGenerator, "energy_collectors", 
                ModBlocks.ENERGY_COLLECTOR_MK1, ModBlocks.ENERGY_COLLECTOR_MK2, ModBlocks.ENERGY_COLLECTOR_MK3);
        }
    
        public void registerCubeAllBlockModels(BlockStateModelGenerator blockStateModelGenerator, Block... blocks) {
            for (int i = 0; i < blocks.length; i++)
                blockStateModelGenerator.registerSimpleCubeAll(blocks[i]);
        }

        public void registerLeveledHorizontalOrientables(BlockStateModelGenerator blockStateModelGenerator, String path, Block... blocks) {
            Identifier id = new Identifier(FabricatedExchange.MOD_ID, "block/" + path + "/");

            for (int i = 0; i < blocks.length; i++) {
                Block block = blocks[i];
                TextureMap textureMap = new TextureMap()
                    .put(TextureKey.TOP, id.withSuffixedPath("top_" + (i + 1)))
                    .put(TextureKey.BOTTOM, id.withSuffixedPath("other"))
                    .put(TextureKey.SIDE, id.withSuffixedPath("other"))
                    .put(TextureKey.FRONT, id.withSuffixedPath("front"));
                Identifier identifier = Models.ORIENTABLE_WITH_BOTTOM.upload(block, textureMap, blockStateModelGenerator.modelCollector);
                blockStateModelGenerator.blockStateCollector.accept(VariantsBlockStateSupplier.create(block, BlockStateVariant.create().put(VariantSettings.MODEL, identifier)).coordinate(BlockStateModelGenerator.createNorthDefaultHorizontalRotationStates()));        
            }
        }


        @Override
        public void generateItemModels(ItemModelGenerator itemModelGenerator) {
            registerGeneratedModels(itemModelGenerator, 
                ModItems.PHILOSOPHERS_STONE, ModItems.ALCHEMICAL_COAL, ModItems.RADIANT_COAL, ModItems.MOBIUS_FUEL, 
                ModItems.AETERNALIS_FUEL, ModItems.LOW_COVALENCE_DUST, ModItems.MEDIUM_COVALENCE_DUST, ModItems.HIGH_COVALENCE_DUST,
                ModItems.DARK_MATTER, ModItems.RED_MATTER, ModItems.TOME_OF_KNOWLEDGE, ModItems.TRANSMUTATION_TABLET);
            registerHandheldModels(itemModelGenerator, ModItems.DARK_MATTER_SWORD, ModItems.DARK_MATTER_PICKAXE, ModItems.DARK_MATTER_AXE, 
                ModItems.DARK_MATTER_SHOVEL, ModItems.DARK_MATTER_HOE);

        }

        public void registerGeneratedModels(ItemModelGenerator itemModelGenerator, Item... items) {
            for (int i = 0; i < items.length; i++) {
                itemModelGenerator.register(items[i], Models.GENERATED);
            }
        }
        public void registerHandheldModels(ItemModelGenerator itemModelGenerator, Item... items) {
            for (int i = 0; i < items.length; i++) {
                itemModelGenerator.register(items[i], Models.HANDHELD);
            }
        }

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

            generateToAndFromBlockRecipes(ModItems.DARK_MATTER, ModBlocks.DARK_MATTER_BLOCK, exporter);
            generateToAndFromBlockRecipes(ModItems.RED_MATTER, ModBlocks.RED_MATTER_BLOCK, exporter);

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

            ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.PHILOSOPHERS_STONE)
                .pattern("GRG")
                .pattern("RDR")
                .pattern("GRG")
                .input('D', Items.DIAMOND)
                .input('R', Items.REDSTONE)
                .input('G', Items.GLOWSTONE_DUST)
                .criterion(FabricRecipeProvider.hasItem(Items.DIAMOND), FabricRecipeProvider.conditionsFromItem(Items.DIAMOND))
                .criterion(FabricRecipeProvider.hasItem(Items.REDSTONE), FabricRecipeProvider.conditionsFromItem(Items.REDSTONE))
                .offerTo(exporter, "philosophers_stone_alt");


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


            ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModBlocks.ALCHEMICAL_CHEST)
                .pattern("LMH")
                .pattern("SDS") 
                .pattern("ICI")
                .input('L', ModItems.LOW_COVALENCE_DUST)
                .input('M', ModItems.MEDIUM_COVALENCE_DUST)
                .input('H', ModItems.HIGH_COVALENCE_DUST)
                .input('D', diamond)
                .input('I', iron)
                .input('C', Blocks.CHEST)
                .input('S', Blocks.STONE)
                .criterion(FabricRecipeProvider.hasItem(ModItems.LOW_COVALENCE_DUST), 
                FabricRecipeProvider.conditionsFromItem(ModItems.LOW_COVALENCE_DUST))
                .criterion(FabricRecipeProvider.hasItem(ModItems.MEDIUM_COVALENCE_DUST), 
                FabricRecipeProvider.conditionsFromItem(ModItems.MEDIUM_COVALENCE_DUST))
                .criterion(FabricRecipeProvider.hasItem(ModItems.HIGH_COVALENCE_DUST), 
                FabricRecipeProvider.conditionsFromItem(ModItems.HIGH_COVALENCE_DUST))
                .offerTo(exporter);


            ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModBlocks.ENERGY_CONDENSER_MK1)
                .pattern("ODO")
                .pattern("DAD") // dad
                .pattern("ODO")
                .input('D', diamond)
                .input('O', Blocks.OBSIDIAN)
                .input('A', ModBlocks.ALCHEMICAL_CHEST)
                .criterion(FabricRecipeProvider.hasItem(ModBlocks.ALCHEMICAL_CHEST), 
                FabricRecipeProvider.conditionsFromItem(ModBlocks.ALCHEMICAL_CHEST))
                .offerTo(exporter);

            ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModBlocks.ENERGY_CONDENSER_MK2)
                .pattern("RDR")
                .pattern("DED") // ded
                .pattern("RDR")
                .input('D', ModBlocks.DARK_MATTER_BLOCK)
                .input('R', ModBlocks.RED_MATTER_BLOCK)
                .input('E', ModBlocks.ENERGY_CONDENSER_MK1)
                .criterion(FabricRecipeProvider.hasItem(ModBlocks.ENERGY_CONDENSER_MK1), 
                FabricRecipeProvider.conditionsFromItem(ModBlocks.ENERGY_CONDENSER_MK1))
                .offerTo(exporter);


            ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModBlocks.ANTIMATTER_RELAY_MK1)
                .pattern("OGO")
                .pattern("ODO") 
                .pattern("OOO")
                .input('G', Blocks.GLASS)
                .input('D', Blocks.DIAMOND_BLOCK)
                .input('O', Blocks.OBSIDIAN)
                .criterion(FabricRecipeProvider.hasItem(Blocks.OBSIDIAN),
                FabricRecipeProvider.conditionsFromItem(Blocks.OBSIDIAN))
                .criterion(FabricRecipeProvider.hasItem(Items.DIAMOND),
                FabricRecipeProvider.conditionsFromItem(Items.DIAMOND))
                .offerTo(exporter);
            ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModBlocks.ANTIMATTER_RELAY_MK2)
                .pattern("ODO")
                .pattern("OAO")
                .pattern("OOO")
                .input('A', ModBlocks.ANTIMATTER_RELAY_MK1)
                .input('D', ModItems.DARK_MATTER)
                .input('O', Blocks.OBSIDIAN)
                .criterion(FabricRecipeProvider.hasItem(ModBlocks.ANTIMATTER_RELAY_MK1),
                FabricRecipeProvider.conditionsFromItem(ModBlocks.ANTIMATTER_RELAY_MK1))
                .offerTo(exporter);

            ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModBlocks.ANTIMATTER_RELAY_MK3)
                .pattern("ORO")
                .pattern("OAO")
                .pattern("OOO")
                .input('A', ModBlocks.ANTIMATTER_RELAY_MK2)
                .input('R', ModItems.RED_MATTER)
                .input('O', Blocks.OBSIDIAN)
                .criterion(FabricRecipeProvider.hasItem(ModBlocks.ANTIMATTER_RELAY_MK2),
                FabricRecipeProvider.conditionsFromItem(ModBlocks.ANTIMATTER_RELAY_MK2))
                .offerTo(exporter);


            ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModBlocks.ENERGY_COLLECTOR_MK1)
                .pattern("GTG")
                .pattern("GDG") 
                .pattern("GFG")
                .input('G', Blocks.GLOWSTONE)
                .input('T', Blocks.GLASS)
                .input('D', Blocks.DIAMOND_BLOCK)
                .input('F', Blocks.FURNACE)
                .criterion(FabricRecipeProvider.hasItem(Blocks.GLOWSTONE),
                FabricRecipeProvider.conditionsFromItem(Blocks.GLOWSTONE))
                .criterion(FabricRecipeProvider.hasItem(Items.DIAMOND),
                FabricRecipeProvider.conditionsFromItem(Items.DIAMOND))
                .offerTo(exporter);

            ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModBlocks.ENERGY_COLLECTOR_MK2)
                .pattern("GDG")
                .pattern("GEG") 
                .pattern("GGG")
                .input('G', Blocks.GLOWSTONE)
                .input('D', ModItems.DARK_MATTER)
                .input('E', ModBlocks.ANTIMATTER_RELAY_MK1)
                .criterion(FabricRecipeProvider.hasItem(ModBlocks.ENERGY_COLLECTOR_MK1),
                FabricRecipeProvider.conditionsFromItem(ModBlocks.ENERGY_COLLECTOR_MK1))
                .offerTo(exporter);

            ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModBlocks.ENERGY_COLLECTOR_MK3)
                .pattern("GRG")
                .pattern("GEG") 
                .pattern("GGG")
                .input('G', Blocks.GLOWSTONE)
                .input('R', ModItems.DARK_MATTER)
                .input('E', ModBlocks.ANTIMATTER_RELAY_MK2)
                .criterion(FabricRecipeProvider.hasItem(ModBlocks.ENERGY_COLLECTOR_MK2),
                FabricRecipeProvider.conditionsFromItem(ModBlocks.ENERGY_COLLECTOR_MK2))
                .offerTo(exporter);

            ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.DARK_MATTER)
                .pattern("AAA")
                .pattern("ADA") 
                .pattern("AAA")
                .input('A', ModItems.AETERNALIS_FUEL)
                .input('D', Blocks.DIAMOND_BLOCK)
                .criterion(FabricRecipeProvider.hasItem(ModItems.AETERNALIS_FUEL),
                FabricRecipeProvider.conditionsFromItem(ModItems.AETERNALIS_FUEL))
                .criterion(FabricRecipeProvider.hasItem(Items.DIAMOND),
                FabricRecipeProvider.conditionsFromItem(Items.DIAMOND))
                .offerTo(exporter);

            ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.RED_MATTER)
                .pattern("AAA")
                .pattern("DDD") 
                .pattern("AAA")
                .input('A', ModItems.AETERNALIS_FUEL)
                .input('D', ModItems.DARK_MATTER)
                .criterion(FabricRecipeProvider.hasItem(ModItems.DARK_MATTER),
                FabricRecipeProvider.conditionsFromItem(ModItems.DARK_MATTER))
                .offerTo(exporter, "red_matter_horizontal");


            ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.RED_MATTER)
                .pattern("ADA")
                .pattern("ADA") 
                .pattern("ADA")
                .input('A', ModItems.AETERNALIS_FUEL)
                .input('D', ModItems.DARK_MATTER)
                .criterion(FabricRecipeProvider.hasItem(ModItems.DARK_MATTER),
                FabricRecipeProvider.conditionsFromItem(ModItems.DARK_MATTER))
                .offerTo(exporter, "red_matter_vertical");


            generateSwordRecipe(ModItems.DARK_MATTER_SWORD, Items.DIAMOND, ModItems.DARK_MATTER, exporter);
        }


        private void generateSwordRecipe(Item sword, Item stick, Item material, Consumer<RecipeJsonProvider> exporter) {
            ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, sword)
                .pattern("M")
                .pattern("M")
                .pattern("S")
                .input('M', material)
                .input('S', stick)
                .criterion(FabricRecipeProvider.hasItem(material), 
                    FabricRecipeProvider.conditionsFromItem(material))
                .criterion(FabricRecipeProvider.hasItem(stick), 
                    FabricRecipeProvider.conditionsFromItem(stick))
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
                .criterion(FabricRecipeProvider.hasItem(item), 
                    FabricRecipeProvider.conditionsFromItem(item))
                .offerTo(exporter, itemName + "_from_block");
                    
            ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, block)
                .input(item, 9)
                .criterion(FabricRecipeProvider.hasItem(item), 
                    FabricRecipeProvider.conditionsFromItem(item))
                .offerTo(exporter, blockName + "_from_item");
        }


    }   
    


}



