package com.skirlez.fabricatedexchange;

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
import net.minecraft.data.client.*;
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

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;


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
			addItemsToTag(ModTags.FUEL,
				Items.COAL, Items.CHARCOAL, Items.REDSTONE, Items.REDSTONE_BLOCK,
				Items.GUNPOWDER, Items.COAL_BLOCK, Items.BLAZE_POWDER, Items.GLOWSTONE_DUST, Items.GLOWSTONE,
				ModItems.ALCHEMICAL_COAL, ModBlocks.ALCHEMICAL_COAL_BLOCK, ModItems.RADIANT_COAL, ModBlocks.RADIANT_COAL_BLOCK,
				ModItems.MOBIUS_FUEL, ModBlocks.MOBIUS_FUEL_BLOCK, ModItems.AETERNALIS_FUEL, ModBlocks.AETERNALIS_FUEL_BLOCK);

			addItemsToTag(ModTags.STONE,
				Items.STONE);
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
				ModItems.AETERNALIS_FUEL, ModItems.LOW_COVALENCE_DUST, ModItems.MEDIUM_COVALENCE_DUST, 
				ModItems.HIGH_COVALENCE_DUST, ModItems.LOW_DIVINING_ROD, ModItems.MEDIUM_DIVINING_ROD, ModItems.HIGH_DIVINING_ROD,
				ModItems.DARK_MATTER, ModItems.RED_MATTER, ModItems.TOME_OF_KNOWLEDGE,
				ModItems.TRANSMUTATION_TABLET, ModItems.IRON_BAND, ModItems.ITEM_ORB, ModItems.DARK_MATTER_BOOTS, 
				ModItems.DARK_MATTER_LEGGINGS, ModItems.DARK_MATTER_CHESTPLATE, ModItems.DARK_MATTER_HELMET,
				ModItems.RED_MATTER_BOOTS, ModItems.RED_MATTER_LEGGINGS, ModItems.RED_MATTER_CHESTPLATE,
				ModItems.RED_MATTER_HELMET, ModItems.REPAIR_TALISMAN, ModItems.EVERTIDE_AMULET,
				ModItems.VOLCANITE_AMULET, ModItems.WATER_ORB, ModItems.LAVA_ORB, ModItems.TORNADO_ORB, ModItems.FROZEN_ORB);
			
			registerHandheldModels(itemModelGenerator, ModItems.DARK_MATTER_SWORD, ModItems.DARK_MATTER_PICKAXE, ModItems.DARK_MATTER_AXE,
				ModItems.DARK_MATTER_SHOVEL, ModItems.DARK_MATTER_HOE, ModItems.DARK_MATTER_HAMMER, ModItems.RED_MATTER_SWORD, ModItems.RED_MATTER_PICKAXE,
				ModItems.RED_MATTER_AXE, ModItems.RED_MATTER_HOE, ModItems.RED_MATTER_SHOVEL, ModItems.RED_MATTER_HAMMER);


			registerGeneratedModels(itemModelGenerator, new String[] {"off", "on"},
				ModItems.IGNITION_RING, ModItems.HYDRATION_RING, ModItems.ZERO_RING, ModItems.GEM_OF_ETERNAL_DENSITY,
				ModItems.BLACK_HOLE_BAND, ModItems.ARCHANGELS_SMITE);

			registerGeneratedModels(itemModelGenerator, new String[] {"off", "on", "off_repelling", "on_repelling"},
				ModItems.SWIFTWOLFS_RENDING_GALE);
		}

		public void registerGeneratedModels(ItemModelGenerator itemModelGenerator, Item... items) {
			for (int i = 0; i < items.length; i++) {
				itemModelGenerator.register(items[i], Models.GENERATED);
			}
		}

		public void registerGeneratedModels(ItemModelGenerator itemModelGenerator, String[] suffixes, Item... items) {
			for (Item item : items) {
				for (String suffix : suffixes) {
					itemModelGenerator.register(item, "_" + suffix, Models.GENERATED);
				}
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

			TagKey<Item> diamond = TagKey.of(RegistryKeys.ITEM, new Identifier("c", "diamonds"));
			TagKey<Item> redstone = TagKey.of(RegistryKeys.ITEM, new Identifier("c", "redstone_dusts"));

			ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.PHILOSOPHERS_STONE)
				.pattern("RGR")
				.pattern("GDG")
				.pattern("RGR")
				.input('D', diamond)
				.input('R', redstone)
				.input('G', Items.GLOWSTONE_DUST)
				.criterion("has_diamond", FabricRecipeProvider.conditionsFromTag(diamond))
				.offerTo(exporter, new Identifier(FabricatedExchange.MOD_ID, "philosophers_stone"));

			ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.PHILOSOPHERS_STONE)
				.pattern("GRG")
				.pattern("RDR")
				.pattern("GRG")
				.input('D', diamond)
				.input('R', redstone)
				.input('G', Items.GLOWSTONE_DUST)
				.criterion("has_diamond", FabricRecipeProvider.conditionsFromTag(diamond))
				.offerTo(exporter, new Identifier(FabricatedExchange.MOD_ID, "philosophers_stone_alt"));

			ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.TRANSMUTATION_TABLET)
				.pattern("DSD")
				.pattern("STS")
				.pattern("DSD")
				.input('D', ModBlocks.DARK_MATTER_BLOCK)
				.input('S', ModTags.STONE)
				.input('T', ModBlocks.TRANSMUTATION_TABLE)
				.criterion(FabricRecipeProvider.hasItem(ModBlocks.DARK_MATTER_BLOCK), FabricRecipeProvider.conditionsFromItem(ModBlocks.DARK_MATTER_BLOCK))
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
				.input(Items.CHARCOAL)
				.input(Items.COBBLESTONE, 8)
				.criterion("has_coal", FabricRecipeProvider.conditionsFromTag(coal))
				.offerTo(exporter);


			TagKey<Item> iron = TagKey.of(RegistryKeys.ITEM, new Identifier("c", "iron_ingots"));

			ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.MEDIUM_COVALENCE_DUST, 40)
				.input(iron)
				.input(redstone)
				.criterion("has_iron", FabricRecipeProvider.conditionsFromTag(iron))
				.criterion("has_redstone", FabricRecipeProvider.conditionsFromTag(redstone))
				.offerTo(exporter);



			ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.HIGH_COVALENCE_DUST, 40)
				.input(diamond)
				.input(Items.COAL)
				.input(Items.COAL)
				.input(Items.COAL)
				.input(Items.COAL)
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
				.input('S', ModTags.STONE)
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
				.criterion(FabricRecipeProvider.hasItem(Items.GLOWSTONE),
					FabricRecipeProvider.conditionsFromItem(Items.GLOWSTONE))
				.criterion(FabricRecipeProvider.hasItem(Items.GLOWSTONE_DUST),
					FabricRecipeProvider.conditionsFromItem(Items.GLOWSTONE_DUST))
				.offerTo(exporter);

			ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModBlocks.ENERGY_COLLECTOR_MK2)
				.pattern("GDG")
				.pattern("GEG")
				.pattern("GGG")
				.input('G', Blocks.GLOWSTONE)
				.input('D', ModItems.DARK_MATTER)
				.input('E', ModBlocks.ENERGY_COLLECTOR_MK1)
				.criterion(FabricRecipeProvider.hasItem(ModBlocks.ENERGY_COLLECTOR_MK1),
				FabricRecipeProvider.conditionsFromItem(ModBlocks.ENERGY_COLLECTOR_MK1))
				.offerTo(exporter);

			ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModBlocks.ENERGY_COLLECTOR_MK3)
				.pattern("GRG")
				.pattern("GEG")
				.pattern("GGG")
				.input('G', Blocks.GLOWSTONE)
				.input('R', ModItems.RED_MATTER)
				.input('E', ModBlocks.ENERGY_COLLECTOR_MK2)
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
				.offerTo(exporter, new Identifier(FabricatedExchange.MOD_ID, "red_matter_horizontal"));


			ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.RED_MATTER)
				.pattern("ADA")
				.pattern("ADA")
				.pattern("ADA")
				.input('A', ModItems.AETERNALIS_FUEL)
				.input('D', ModItems.DARK_MATTER)
				.criterion(FabricRecipeProvider.hasItem(ModItems.DARK_MATTER),
				FabricRecipeProvider.conditionsFromItem(ModItems.DARK_MATTER))
				.offerTo(exporter, new Identifier(FabricatedExchange.MOD_ID, "red_matter_vertical"));

			ShapedRecipeJsonBuilder.create(RecipeCategory.COMBAT, ModItems.DARK_MATTER_SWORD)
				.pattern("M")
				.pattern("M")
				.pattern("D")
				.input('M', ModItems.DARK_MATTER)
				.input('D', diamond)
				.criterion(FabricRecipeProvider.hasItem(ModItems.DARK_MATTER),
					FabricRecipeProvider.conditionsFromItem(ModItems.DARK_MATTER))
				.offerTo(exporter);

			ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, ModItems.DARK_MATTER_PICKAXE)
				.pattern("MMM")
				.pattern(" D ")
				.pattern(" D ")
				.input('M', ModItems.DARK_MATTER)
				.input('D', diamond)
				.criterion(FabricRecipeProvider.hasItem(ModItems.DARK_MATTER),
					FabricRecipeProvider.conditionsFromItem(ModItems.DARK_MATTER))
				.offerTo(exporter);

			ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, ModItems.DARK_MATTER_SHOVEL)
				.pattern("M")
				.pattern("D")
				.pattern("D")
				.input('M', ModItems.DARK_MATTER)
				.input('D', diamond)
				.criterion(FabricRecipeProvider.hasItem(ModItems.DARK_MATTER),
					FabricRecipeProvider.conditionsFromItem(ModItems.DARK_MATTER))
				.offerTo(exporter);

			ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, ModItems.DARK_MATTER_AXE)
				.pattern("MM")
				.pattern("MD")
				.pattern(" D")
				.input('M', ModItems.DARK_MATTER)
				.input('D', diamond)
				.criterion(FabricRecipeProvider.hasItem(ModItems.DARK_MATTER),
					FabricRecipeProvider.conditionsFromItem(ModItems.DARK_MATTER))
				.offerTo(exporter);

			ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, ModItems.DARK_MATTER_HOE)
				.pattern("MM")
				.pattern(" D")
				.pattern(" D")
				.input('M', ModItems.DARK_MATTER)
				.input('D', diamond)
				.criterion(FabricRecipeProvider.hasItem(ModItems.DARK_MATTER),
					FabricRecipeProvider.conditionsFromItem(ModItems.DARK_MATTER))
				.offerTo(exporter);

			ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, ModItems.DARK_MATTER_HAMMER)
				.pattern("MDM")
				.pattern(" D ")
				.pattern(" D ")
				.input('M', ModItems.DARK_MATTER)
				.input('D', diamond)
				.criterion(FabricRecipeProvider.hasItem(ModItems.DARK_MATTER_HAMMER),
						FabricRecipeProvider.conditionsFromItem(ModItems.DARK_MATTER_HAMMER))
				.offerTo(exporter);
			
			
			
			
			ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, ModItems.RED_MATTER_SWORD)
				.pattern("M")
				.pattern("M")
				.pattern("D")
				.input('M', ModItems.RED_MATTER)
				.input('D', ModItems.DARK_MATTER_SWORD)
				.criterion(FabricRecipeProvider.hasItem(ModItems.DARK_MATTER_SWORD),
						FabricRecipeProvider.conditionsFromItem(ModItems.RED_MATTER))
				.offerTo(exporter);

			ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, ModItems.RED_MATTER_PICKAXE)
				.pattern("MMM")
				.pattern(" D ")
				.pattern(" C ")
				.input('M', ModItems.RED_MATTER)
				.input('D', ModItems.DARK_MATTER_PICKAXE)
				.input('C', ModItems.DARK_MATTER)
				.criterion(FabricRecipeProvider.hasItem(ModItems.DARK_MATTER_PICKAXE),
						FabricRecipeProvider.conditionsFromItem(ModItems.RED_MATTER))
				.offerTo(exporter);

			ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, ModItems.RED_MATTER_AXE)
				.pattern("MM")
				.pattern("MD")
				.pattern(" C")
				.input('M', ModItems.RED_MATTER)
				.input('D', ModItems.DARK_MATTER_AXE)
				.input('C', ModItems.DARK_MATTER)
				.criterion(FabricRecipeProvider.hasItem(ModItems.DARK_MATTER_AXE),
						FabricRecipeProvider.conditionsFromItem(ModItems.RED_MATTER))
				.offerTo(exporter);

			ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, ModItems.RED_MATTER_HOE)
				.pattern("MM")
				.pattern(" D")
				.pattern(" C")
				.input('M', ModItems.RED_MATTER)
				.input('D', ModItems.DARK_MATTER_HOE)
				.input('C', ModItems.DARK_MATTER)
				.criterion(FabricRecipeProvider.hasItem(ModItems.DARK_MATTER_HOE),
						FabricRecipeProvider.conditionsFromItem(ModItems.RED_MATTER))
				.offerTo(exporter);

			ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, ModItems.RED_MATTER_SHOVEL)
				.pattern("M")
				.pattern("D")
				.pattern("C")
				.input('M', ModItems.RED_MATTER)
				.input('D', ModItems.DARK_MATTER_SHOVEL)
				.input('C', ModItems.DARK_MATTER)
				.criterion(FabricRecipeProvider.hasItem(ModItems.DARK_MATTER_SHOVEL),
						FabricRecipeProvider.conditionsFromItem(ModItems.RED_MATTER))
				.offerTo(exporter);

			ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, ModItems.RED_MATTER_HAMMER)
				.pattern("RDR")
				.pattern(" H ")
				.pattern(" D ")
				.input('R', ModItems.RED_MATTER)
				.input('H', ModItems.DARK_MATTER_HAMMER)
				.input('D', ModItems.DARK_MATTER)
				.criterion(FabricRecipeProvider.hasItem(ModItems.DARK_MATTER_HAMMER),
						FabricRecipeProvider.conditionsFromItem(ModItems.DARK_MATTER_HAMMER))
				.offerTo(exporter);
			
			
			
			
			ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.IRON_BAND)
				.pattern("III")
				.pattern("ILI")
				.pattern("III")
				.input('L', Items.LAVA_BUCKET)
				.input('I', Items.IRON_INGOT)
				.criterion(FabricRecipeProvider.hasItem(Items.IRON_INGOT),
						FabricRecipeProvider.conditionsFromItem(Items.IRON_INGOT))
				.offerTo(exporter);

			ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.SWIFTWOLFS_RENDING_GALE)
				.pattern("DFD")
				.pattern("FBF")
				.pattern("DFD")
				.input('D', ModItems.DARK_MATTER)
				.input('F', Items.FEATHER)
				.input('B', ModItems.IRON_BAND)
				.criterion(FabricRecipeProvider.hasItem(ModItems.IRON_BAND),
						FabricRecipeProvider.conditionsFromItem(ModItems.IRON_BAND))
				.criterion(FabricRecipeProvider.hasItem(ModItems.DARK_MATTER),
						FabricRecipeProvider.conditionsFromItem(ModItems.DARK_MATTER))
				.offerTo(exporter);
			
			ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.ARCHANGELS_SMITE)
				.pattern("BFB")
				.pattern("DID")
				.pattern("BFB")
				.input('D', ModItems.DARK_MATTER)
				.input('I', ModItems.IRON_BAND)
				.input('F', Items.FEATHER)
				.input('B', Items.BOW)
				.criterion(FabricRecipeProvider.hasItem(ModItems.IRON_BAND),
						FabricRecipeProvider.conditionsFromItem(ModItems.IRON_BAND))
				.criterion(FabricRecipeProvider.hasItem(ModItems.DARK_MATTER),
						FabricRecipeProvider.conditionsFromItem(ModItems.DARK_MATTER))
				.offerTo(exporter);

			ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.REPAIR_TALISMAN)
					.pattern("LMH")
					.pattern("SPS")
					.pattern("HML")
					.input('L', ModItems.LOW_COVALENCE_DUST)
					.input('M', ModItems.MEDIUM_COVALENCE_DUST)
					.input('H', ModItems.HIGH_COVALENCE_DUST)
					.input('S', Items.STRING)
					.input('P', Items.PAPER)
					.criterion(FabricRecipeProvider.hasItem(ModItems.LOW_COVALENCE_DUST),
							FabricRecipeProvider.conditionsFromItem(ModItems.LOW_COVALENCE_DUST))
					.criterion(FabricRecipeProvider.hasItem(ModItems.MEDIUM_COVALENCE_DUST),
							FabricRecipeProvider.conditionsFromItem(ModItems.MEDIUM_COVALENCE_DUST))
					.criterion(FabricRecipeProvider.hasItem(ModItems.HIGH_COVALENCE_DUST),
							FabricRecipeProvider.conditionsFromItem(ModItems.HIGH_COVALENCE_DUST))
					.offerTo(exporter);

			ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.ZERO_RING)
				.pattern("BSB")
				.pattern("DID")
				.pattern("BSB")
				.input('S', Items.SNOWBALL)
				.input('B', Items.SNOW)
				.input('D', ModItems.DARK_MATTER)
				.input('I', ModItems.IRON_BAND)
				.criterion(FabricRecipeProvider.hasItem(ModItems.DARK_MATTER),
					FabricRecipeProvider.conditionsFromItem(ModItems.DARK_MATTER))
				.offerTo(exporter);

			ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.IGNITION_RING)
				.pattern("FMF")
				.pattern("DID")
				.pattern("FMF")
				.input('F', Items.FLINT_AND_STEEL)
				.input('M', ModItems.MOBIUS_FUEL)
				.input('D', ModItems.DARK_MATTER)
				.input('I', ModItems.IRON_BAND)
				.criterion(FabricRecipeProvider.hasItem(ModItems.DARK_MATTER),
					FabricRecipeProvider.conditionsFromItem(ModItems.DARK_MATTER))
				.offerTo(exporter);


			ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.HYDRATION_RING)
				.input(ModItems.IGNITION_RING)
				.input(ModItems.ZERO_RING)
				.criterion(FabricRecipeProvider.hasItem(ModItems.IGNITION_RING),
					FabricRecipeProvider.conditionsFromItem(ModItems.IGNITION_RING))
				.criterion(FabricRecipeProvider.hasItem(ModItems.ZERO_RING),
					FabricRecipeProvider.conditionsFromItem(ModItems.ZERO_RING))
				.offerTo(exporter);


			ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.LOW_DIVINING_ROD)
				.pattern("DDD")
				.pattern("DSD")
				.pattern("DDD")
				.input('S', Items.STICK)
				.input('D', ModItems.LOW_COVALENCE_DUST)
				.criterion(FabricRecipeProvider.hasItem(ModItems.LOW_COVALENCE_DUST),
					FabricRecipeProvider.conditionsFromItem(ModItems.LOW_COVALENCE_DUST))
				.offerTo(exporter);
			ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.MEDIUM_DIVINING_ROD)
				.pattern("DDD")
				.pattern("DRD")
				.pattern("DDD")
				.input('R', ModItems.LOW_DIVINING_ROD)
				.input('D', ModItems.MEDIUM_COVALENCE_DUST)
				.criterion(FabricRecipeProvider.hasItem(ModItems.MEDIUM_COVALENCE_DUST),
					FabricRecipeProvider.conditionsFromItem(ModItems.MEDIUM_COVALENCE_DUST))
				.offerTo(exporter);
			ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.HIGH_DIVINING_ROD)
				.pattern("DDD")
				.pattern("DRD")
				.pattern("DDD")
				.input('R', ModItems.MEDIUM_DIVINING_ROD)
				.input('D', ModItems.HIGH_COVALENCE_DUST)
				.criterion(FabricRecipeProvider.hasItem(ModItems.HIGH_COVALENCE_DUST),
					FabricRecipeProvider.conditionsFromItem(ModItems.HIGH_COVALENCE_DUST))
				.offerTo(exporter);




			ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.GEM_OF_ETERNAL_DENSITY)
					.pattern("FAF")
					.pattern("DFD")
					.pattern("FAF")
					.input('A', Items.DIAMOND)
					.input('D', ModItems.DARK_MATTER)
					.input('F', Items.OBSIDIAN)
					.criterion(FabricRecipeProvider.hasItem(ModItems.DARK_MATTER),
							FabricRecipeProvider.conditionsFromItem(ModItems.DARK_MATTER))
					.offerTo(exporter);

			ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.BLACK_HOLE_BAND)
					.pattern("SSS")
					.pattern("DBD")
					.pattern("SSS")
					.input('S', Items.STRING)
					.input('D', ModItems.DARK_MATTER)
					.input('B', ModItems.IRON_BAND)
					.criterion(FabricRecipeProvider.hasItem(ModItems.DARK_MATTER),
							FabricRecipeProvider.conditionsFromItem(ModItems.DARK_MATTER))
					.offerTo(exporter);

			ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.EVERTIDE_AMULET)
					.pattern("WWW")
					.pattern("DDD")
					.pattern("WWW")
					.input('W', Items.WATER_BUCKET)
					.input('D', ModItems.DARK_MATTER)
					.criterion(FabricRecipeProvider.hasItem(ModItems.DARK_MATTER),
							FabricRecipeProvider.conditionsFromItem(ModItems.DARK_MATTER))
					.offerTo(exporter);

			ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.VOLCANITE_AMULET)
					.pattern("LLL")
					.pattern("DDD")
					.pattern("LLL")
					.input('L', Items.LAVA_BUCKET)
					.input('D', ModItems.DARK_MATTER)
					.criterion(FabricRecipeProvider.hasItem(ModItems.DARK_MATTER),
							FabricRecipeProvider.conditionsFromItem(ModItems.DARK_MATTER))
					.offerTo(exporter);

			generateArmorRecipes(ModItems.DARK_MATTER, ModItems.DARK_MATTER_BOOTS, ModItems.DARK_MATTER_LEGGINGS,
				ModItems.DARK_MATTER_CHESTPLATE, ModItems.DARK_MATTER_HELMET, exporter);

			generateArmorRecipes(ModItems.RED_MATTER, ModItems.RED_MATTER_BOOTS, ModItems.RED_MATTER_LEGGINGS,
					ModItems.RED_MATTER_CHESTPLATE, ModItems.RED_MATTER_HELMET, exporter);
		}

		private void generatePhilosopherStoneRecipe(Item item1, Item item2, int ratio, Consumer<RecipeJsonProvider> exporter) {
			String name1 = Registries.ITEM.getId(item1).getPath();
			String name2 = Registries.ITEM.getId(item2).getPath();
			ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, item2)
				.input(ModItems.PHILOSOPHERS_STONE)
				.input(item1, ratio)
				.criterion(FabricRecipeProvider.hasItem(ModItems.PHILOSOPHERS_STONE),
					FabricRecipeProvider.conditionsFromItem(ModItems.PHILOSOPHERS_STONE))
				.offerTo(exporter, new Identifier(FabricatedExchange.MOD_ID, "ps_" + name2 + "_from_" + name1));

			ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, item1, ratio)
				.input(ModItems.PHILOSOPHERS_STONE)
				.input(item2)
				.criterion(FabricRecipeProvider.hasItem(ModItems.PHILOSOPHERS_STONE),
					FabricRecipeProvider.conditionsFromItem(ModItems.PHILOSOPHERS_STONE))
				.offerTo(exporter, new Identifier(FabricatedExchange.MOD_ID, "ps_" + name1 + "_from_" + name2));
		}

		private void generateToAndFromBlockRecipes(Item item, Block block, Consumer<RecipeJsonProvider> exporter) {
			String itemName = Registries.ITEM.getId(item).getPath();
			String blockName = Registries.ITEM.getId(block.asItem()).getPath();
			ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, item, 9)
				.input(block)
				.criterion(FabricRecipeProvider.hasItem(item),
					FabricRecipeProvider.conditionsFromItem(item))
				.offerTo(exporter, new Identifier(FabricatedExchange.MOD_ID, itemName + "_from_block"));

			ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, block)
				.input(item, 9)
				.criterion(FabricRecipeProvider.hasItem(item),
					FabricRecipeProvider.conditionsFromItem(item))
				.offerTo(exporter, new Identifier(blockName + "_from_item"));
		}
		
		private void generateArmorRecipes(Item material, Item boots, Item leggings, Item chestplate, Item helmet, Consumer<RecipeJsonProvider> exporter) {
			ShapedRecipeJsonBuilder.create(RecipeCategory.COMBAT, boots)
				.pattern("M M")
				.pattern("M M")
				.input('M', material)
				.criterion(FabricRecipeProvider.hasItem(material),
					FabricRecipeProvider.conditionsFromItem(material))
				.offerTo(exporter);
			
			ShapedRecipeJsonBuilder.create(RecipeCategory.COMBAT, leggings)
				.pattern("MMM")
				.pattern("M M")
				.pattern("M M")
				.input('M', material)
				.criterion(FabricRecipeProvider.hasItem(material),
					FabricRecipeProvider.conditionsFromItem(material))
				.offerTo(exporter);
			
			ShapedRecipeJsonBuilder.create(RecipeCategory.COMBAT, chestplate)
				.pattern("M M")
				.pattern("MMM")
				.pattern("MMM")
				.input('M', material)
				.criterion(FabricRecipeProvider.hasItem(material),
					FabricRecipeProvider.conditionsFromItem(material))
				.offerTo(exporter);
			
			ShapedRecipeJsonBuilder.create(RecipeCategory.COMBAT, helmet)
				.pattern("MMM")
				.pattern("M M")
				.input('M', material)
				.criterion(FabricRecipeProvider.hasItem(material),
					FabricRecipeProvider.conditionsFromItem(material))
				.offerTo(exporter);
		}
		
		

	}



}



