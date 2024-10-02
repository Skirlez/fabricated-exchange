package com.skirlez.fabricatedexchange.emc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.mixin.BrewingRecipeAccessor;
import com.skirlez.fabricatedexchange.mixin.BrewingRecipeRegistryAccessor;
import com.skirlez.fabricatedexchange.mixin.IngredientAccessor;
import com.skirlez.fabricatedexchange.mixin.LegacySmithingRecipeAccessor;
import com.skirlez.fabricatedexchange.mixin.TagEntryAccessor;
import com.skirlez.fabricatedexchange.util.GeneralUtil;
import com.skirlez.fabricatedexchange.util.SuperNumber;
import com.skirlez.fabricatedexchange.util.config.EqualTagsFile;
import com.skirlez.fabricatedexchange.util.config.ModDataFiles;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.LegacySmithingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SmeltingRecipe;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.recipe.StonecuttingRecipe;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

/** This class tries to infer EMC values of items, potions and enchantments through in-game recipes. 
 * You create it, giving it a RecipeManager and DynamicRegistryManager,
 * call .map(), and then get the results using getEmcMap(), getPotionEmcMap() and getEnchantmentEmcMap() */

// In areas of code where a possible rare case is not considered, 
// a comment prefixed with "UNCONSIDERED:" should be left

public class EmcMapper {
	private HashMap<Item, SuperNumber> emcMap;
	private HashMap<Potion, SuperNumber> potionEmcMap;
	private HashMap<Enchantment, SuperNumber> enchantmentEmcMap;
	
	private StringBuilder warning = new StringBuilder();
	
	private RecipeManager recipeManager;
	private DynamicRegistryManager dynamicRegistryManager;
	
	private Map<Item, Set<MapperAction>> unknownEquationMap = new HashMap<Item, Set<MapperAction>>();
	private LayeredQueue<MapperAction> queue = new LayeredQueue<MapperAction>(5);

	private Set<MapperAction> queuedActionsSet = new HashSet<MapperAction>();

	private final EqualTagsFile equalTags;
	
	public EmcMapper(RecipeManager recipeManager, DynamicRegistryManager dynamicRegistryManager) {
		this.recipeManager = recipeManager;
		this.dynamicRegistryManager = dynamicRegistryManager;
		
		emcMap = new HashMap<Item, SuperNumber>();
		potionEmcMap = new HashMap<Potion, SuperNumber>();
		enchantmentEmcMap = new HashMap<Enchantment, SuperNumber>();
				
		equalTags = ModDataFiles.EQUAL_TAGS;
	}

	public HashMap<Item, SuperNumber> getEmcMap() {
		return emcMap;
	}
	public HashMap<Potion, SuperNumber> getPotionEmcMap() {
		return potionEmcMap;
	}
	public HashMap<Enchantment, SuperNumber> getEnchantmentEmcMap() {
		return enchantmentEmcMap;
	}
	
	/** Maps out all the recipes known to recipeManager + item equations given by mods. */
	public boolean map() {
		Map<Item, SuperNumber> seedEmcMap = ModDataFiles.SEED_EMC_MAP.getEmcMap();
		if (seedEmcMap != null)
			GeneralUtil.mergeMap(emcMap, seedEmcMap);

		if (ModDataFiles.MAIN_CONFIG_FILE.mapper_enabled) {
			List<SmithingRecipe> allSmithingRecipes = recipeManager.listAllOfType(RecipeType.SMITHING);
			List<SmeltingRecipe> allSmeltingRecipes = recipeManager.listAllOfType(RecipeType.SMELTING);
			List<CraftingRecipe> allCraftingRecipes = recipeManager.listAllOfType(RecipeType.CRAFTING);
			List<StonecuttingRecipe> allStonecuttingRecipes = recipeManager.listAllOfType(RecipeType.STONECUTTING);
			List<BrewingRecipeRegistry.Recipe<Item>> allBrewingRecipes = BrewingRecipeRegistryAccessor.getItemRecipes();
			// blacklisted recipes and items


			convertRecipesToEquations(allSmithingRecipes, "smithing", this::createSmithingEquation, this::getRecipeName);
			convertRecipesToEquations(allSmeltingRecipes, "smelting", this::createSmeltingEquation, this::getRecipeName);
			convertRecipesToEquations(allCraftingRecipes, "crafting", this::createCraftingEquation, this::getRecipeName);
			convertRecipesToEquations(allStonecuttingRecipes, "stonecutting", this::createStonecuttingEquation, this::getRecipeName);
			convertRecipesToEquations(allBrewingRecipes, "brewing", this::createBrewingEquation, this::getBrewingRecipeName);
	
			createSpecialEquations();
			
			// TODO: inject recipes provided by mods here
			List<List<Item>> itemGroups = equalTags.getItemGroups(); 
			for (List<Item> list : itemGroups) {
				MapperAction action = new EqualizeTagAction(list);
				for (Item item : list) {
					registerAction(item, action);
					if (emcMapHasEntry(item))
						action.feed(item);
				}
				if (action.shouldAdd())
					addActionToQueue(action);
			}
	
	
			while (!queue.isEmpty()) {
				MapperAction action = queue.poll();
				action.perform();
			}
			
	
			// most potion recipes are very very special, so we need to take care of them separately.
			// i've decided to just copy this section from the old emc mapper, using the dumber method, 
			// because there are not that many potion recipes.
			potionEmcMap.put(Potions.WATER, new SuperNumber(0));
			List<BrewingRecipeRegistry.Recipe<Potion>> potionRecipes = BrewingRecipeRegistryAccessor.getPotionRecipes();
			for (int i = 0; i < 100; i++) {
				boolean newInfo = false;
				for (BrewingRecipeRegistry.Recipe<Potion> recipe : potionRecipes)
					newInfo = iteratePotionRecipe((BrewingRecipeAccessor<Potion>)recipe) || newInfo;
				if (!newInfo)
					break;
			}
	
			SuperNumber constant = ModDataFiles.MAIN_CONFIG_FILE.enchantmentEmcConstant; 
			for (Enchantment enchantment : Registries.ENCHANTMENT) {
				int max = enchantment.getMaxLevel();
				int weight = enchantment.getRarity().getWeight();
				
				SuperNumber emc = new SuperNumber(constant);
				emc.divide(weight);
	
				if (enchantment.isTreasure())
					emc.multiply(10);
	
				emc.divide(1 << (max - 1));
				enchantmentEmcMap.put(enchantment, emc);
			}
		}
		
		Map<Item, SuperNumber> customEmcMap = ModDataFiles.CUSTOM_EMC_MAP.getEmcMap();
		if (customEmcMap != null)
			GeneralUtil.mergeMap(emcMap, customEmcMap);
		
		if (!warning.isEmpty())
			FabricatedExchange.LOGGER.warn(warning.toString());
		return (!warning.isEmpty());
	}


	private String getRecipeName(Recipe<?> recipe) {
		return recipe.getId().toString();
	}
	private String getBrewingRecipeName(BrewingRecipeRegistry.Recipe<Item> recipe) {
		BrewingRecipeAccessor<Item> accessor = (BrewingRecipeAccessor<Item>)recipe;
		return "brewing:" + Registries.ITEM.getId(accessor.getInput()).getPath() + "_to_" + 
			Registries.ITEM.getId(accessor.getOutput()).getPath() + "_using_" + 
			Registries.ITEM.getId(accessor.getIngredient().getMatchingStacks()[0].getItem()).getPath();
	}
	private String getEquationName(ItemEquation equation) {
		return (equation == null) ? "none" : (equation.origin + ":" + equation.name);
	}

	private ItemEquation createCraftingEquation(CraftingRecipe recipe) {
		List<Ingredient> ingredients = recipe.getIngredients();
		List<ItemStack> output = new ArrayList<ItemStack>(1);
		output.add(recipe.getOutput(this.dynamicRegistryManager));
		List<Ingredient> filteredIngredients = new ArrayList<Ingredient>(ingredients.size());
		for (Ingredient ingredient : ingredients) {
			if (ingredient.getMatchingStacks().length == 0)
				continue;
			ItemStack firstStack = ingredient.getMatchingStacks()[0];
			if (isIngredientRemainderItself(ingredient))
				continue;
			
			ItemStack remainder = firstStack.getRecipeRemainder();
			if (!remainder.isEmpty())
				output.add(remainder);
			filteredIngredients.add(ingredient);
		}
		return new ItemEquation(filteredIngredients, output, recipe.getId());
	}

	private boolean isIngredientRemainderItself(Ingredient ingredient) {
	
		// UNCONSIDERED: ingredients with multiple itemstacks having different recipe remainders
		// (we are only analyzing the first itemstack)
		
		ItemStack[] stacks = ingredient.getMatchingStacks();
		ItemStack remainder = stacks[0].getRecipeRemainder();
		if (remainder.isEmpty())
			return false;
		
		for (ItemStack other : stacks) {
			if (ItemStack.areEqual(remainder, other))
				return true;
		}
		return false;
	}
	
	private ItemEquation createSmeltingEquation(SmeltingRecipe recipe) {
		return new ItemEquation(
			Arrays.asList(recipe.getIngredients().get(0)),
			Arrays.asList(recipe.getOutput(this.dynamicRegistryManager)),
			recipe.getId());
	}

	@Nullable
	private ItemEquation createSmithingEquation(SmithingRecipe recipe) {
		if (!(recipe instanceof LegacySmithingRecipe))
			return null;
		LegacySmithingRecipeAccessor recipeAccessor = (LegacySmithingRecipeAccessor) recipe;
		return new ItemEquation(
			Arrays.asList(recipeAccessor.getBase(), recipeAccessor.getAddition()),
			Arrays.asList(recipe.getOutput(this.dynamicRegistryManager)),
			recipe.getId());
	}

	private ItemEquation createStonecuttingEquation(StonecuttingRecipe recipe) {
		return new ItemEquation(
			recipe.getIngredients(),
			Arrays.asList(recipe.getOutput(this.dynamicRegistryManager)),
			recipe.getId());
	}

	private ItemEquation createBrewingEquation(BrewingRecipeRegistry.Recipe<Item> recipe) {
		BrewingRecipeAccessor<Item> accessor = (BrewingRecipeAccessor<Item>)recipe;
		Item input = accessor.getInput();

		DefaultedList<Ingredient> ingredientList = DefaultedList.of();
		ingredientList.add(accessor.getIngredient());
		ingredientList.add(Ingredient.ofItems(input));
		ingredientList.add(Ingredient.ofItems(input));
		ingredientList.add(Ingredient.ofItems(input));

		String name = getBrewingRecipeName(recipe);

		ItemEquation equation = new ItemEquation(
			ingredientList, 
			Arrays.asList(new ItemStack(accessor.getOutput(), 3)), 
			new Identifier(name));
				
		return equation;
	}

	private <T> void convertRecipesToEquations(List<T> allRecipes, String type, 
			Function<T, ItemEquation> equationConverter, Function<T, String> nameFunction) {

		for (int i = 0; i < allRecipes.size(); i++) {
			T recipe = allRecipes.get(i);
			if (ModDataFiles.BLACKLISTED_MAPPER_RECIPES.isRecipeBlacklisted(nameFunction.apply(recipe), type))
				continue;
			ItemEquation equation = equationConverter.apply(recipe);
			if (equation == null)
				continue;
			processEquation(equation);
		}
	}
	
	private void createSpecialEquations() {
		ItemEquation tippedArrowEquation = new ItemEquation(
				Arrays.asList(Ingredient.ofStacks(new ItemStack(Items.ARROW, 8))), 
				Arrays.asList( new ItemStack(Items.TIPPED_ARROW)),
				new Identifier("fe-equations", "tipped_arrow_base"));
		processEquation(tippedArrowEquation);
		
		createWorldInteractionEquation(Blocks.WHITE_CONCRETE_POWDER, Blocks.WHITE_CONCRETE);
		createWorldInteractionEquation(Blocks.ORANGE_CONCRETE_POWDER, Blocks.ORANGE_CONCRETE);
		createWorldInteractionEquation(Blocks.MAGENTA_CONCRETE_POWDER, Blocks.MAGENTA_CONCRETE);
		createWorldInteractionEquation(Blocks.LIGHT_BLUE_CONCRETE_POWDER, Blocks.LIGHT_BLUE_CONCRETE);
		createWorldInteractionEquation(Blocks.YELLOW_CONCRETE_POWDER, Blocks.YELLOW_CONCRETE);
		createWorldInteractionEquation(Blocks.LIME_CONCRETE_POWDER, Blocks.LIME_CONCRETE);
		createWorldInteractionEquation(Blocks.PINK_CONCRETE_POWDER, Blocks.PINK_CONCRETE);
		createWorldInteractionEquation(Blocks.GRAY_CONCRETE_POWDER, Blocks.GRAY_CONCRETE);
		createWorldInteractionEquation(Blocks.LIGHT_GRAY_CONCRETE_POWDER, Blocks.LIGHT_GRAY_CONCRETE);
		createWorldInteractionEquation(Blocks.CYAN_CONCRETE_POWDER, Blocks.CYAN_CONCRETE);
		createWorldInteractionEquation(Blocks.PURPLE_CONCRETE_POWDER, Blocks.PURPLE_CONCRETE);
		createWorldInteractionEquation(Blocks.BLUE_CONCRETE_POWDER, Blocks.BLUE_CONCRETE);
		createWorldInteractionEquation(Blocks.BROWN_CONCRETE_POWDER, Blocks.BROWN_CONCRETE);
		createWorldInteractionEquation(Blocks.GREEN_CONCRETE_POWDER, Blocks.GREEN_CONCRETE);
		createWorldInteractionEquation(Blocks.RED_CONCRETE_POWDER, Blocks.RED_CONCRETE);
		createWorldInteractionEquation(Blocks.BLACK_CONCRETE_POWDER, Blocks.BLACK_CONCRETE);
		
	}
	
	private void createWorldInteractionEquation(Block input, Block output) {
		createWorldInteractionEquation(input, output, Optional.empty());
	}
	private void createWorldInteractionEquation(Block input, Block output, Optional<Item> using) {
		String inputName = Registries.BLOCK.getId(input).getPath();
		String outputName = Registries.BLOCK.getId(output).getPath();
		
		List<Ingredient> inputList = new ArrayList<Ingredient>();
		inputList.add(Ingredient.ofItems(input));
		if (using.isPresent())
			inputList.add(Ingredient.ofItems(using.get()));
		ItemEquation equation = new ItemEquation(inputList, Arrays.asList(new ItemStack(output)), 
			new Identifier("fe-equations", inputName + "_to_" + outputName));
		
		processEquation(equation);
	}


	private boolean emcMapHasEntry(Item item) {
		return emcMap.containsKey(item);
	}

	private String itemName(Item item) {
		return Registries.ITEM.getId(item).toString();
	}

	// Counts the unknowns of the equation and also simplifies it down if it has ingredients with a tag marked as an equal tag.
	public void processEquation(ItemEquation equation) {

		Set<Item> knownItems = new HashSet<Item>();
		Set<Item> unknownItems = new HashSet<Item>();
		for (int i = 0; i < equation.input.size(); i++) {
			Ingredient ingredient = equation.input.get(i);
			final Ingredient.Entry[] entries = ((IngredientAccessor)(Object)ingredient).getEntries();
			for (Ingredient.Entry entry : entries) {
				if (!(entry instanceof Ingredient.TagEntry))
					continue;
				TagKey<Item> tag = ((TagEntryAccessor)(Object)entry).getTag();
				if (equalTags.hasTag(tag)) {
					Item item = GeneralUtil.getAnyItemFromItemTag(tag).get();
					ItemStack stack = new ItemStack(item, getIngredientCount(ingredient));
					ingredient = Ingredient.ofStacks(stack);
					equation.input.set(i, ingredient);
				}
			}


			for (ItemStack stack : ingredient.getMatchingStacks()) {
				if (!emcMapHasEntry(stack.getItem()))
					unknownItems.add(stack.getItem());
				else
					knownItems.add(stack.getItem());
			}
		}
		for (ItemStack stack : equation.output) {
			if (!emcMapHasEntry(stack.getItem()))
				unknownItems.add(stack.getItem());
			else
				knownItems.add(stack.getItem());
		}   
		int amount = unknownItems.size() + knownItems.size();
		
		MapperAction action = new SolveEquationAction(equation, amount);
		for (Item item : unknownItems)
			registerAction(item, action);
		for (Item item : knownItems)
			action.feed(item);

		if (action.shouldAdd())
			addActionToQueue(action);
	}

	private void addActionToQueue(MapperAction action) {
		queue.add(action, action.getLayer());
		queuedActionsSet.add(action);
	}

	private void registerAction(Item item, MapperAction action) {
		if (!unknownEquationMap.containsKey(item)) {
			Set<MapperAction> set = new HashSet<MapperAction>();
			unknownEquationMap.put(item, set);
		}
		unknownEquationMap.get(item).add(action);
	}

	private void equalizeList(List<Item> items) {
		SuperNumber emc = SuperNumber.ZERO;
		for (Item item : items) {
			if (emcMapHasEntry(item)) {
				emc = getItemEmc(item);
				break;
			}
		}
		if (emc.equalsZero()) {
			warn("Attempted to equalize tag without any items that have EMC! Skipping...");
			return;
		}
		for (Item item : items) {
			if (!emcMapHasEntry(item))
				putEmcMap(item, emc, null);
		}
	}


	private void solve(ItemEquation equation) {
		SuperNumber sum = SuperNumber.Zero();
		Set<Item> unknownItems = new HashSet<Item>();
		int unknownMult = 0;

  
		for (ItemStack stack : equation.output) {
			SuperNumber emc = getItemStackEmc(stack);
			if (emc.equalsZero()) {
				unknownItems.add(stack.getItem());
				unknownMult += stack.getCount();
			}
			else
				sum.subtract(emc);
		}
		for (Ingredient ingredient : equation.input) {
			SuperNumber emc = getIngredientEmc(ingredient);
			if (emc.equalsZero()) {
				for (ItemStack stack : ingredient.getMatchingStacks())
					unknownItems.add(stack.getItem());
				unknownMult -= getIngredientCount(ingredient);
			}
			else
				sum.add(emc);
		}

		if (unknownMult == 0) {
			
			warn("Could not solve weird recipe: " + getEquationName(equation));
			return;
		}

		SuperNumber unknownWorth = new SuperNumber(sum);
		unknownWorth.divide(unknownMult);
		for (Item item : unknownItems) {
			putEmcMap(item, unknownWorth, equation);
		}
	}

	private void verify(ItemEquation equation) {
		SuperNumber inputEmc = SuperNumber.Zero();
		SuperNumber outputEmc = SuperNumber.Zero();

		for (ItemStack stack : equation.output)
			outputEmc.add(getItemStackEmc(stack));

		for (Ingredient ingredient : equation.input)
			inputEmc.add(getIngredientEmc(ingredient));

		if (inputEmc.compareTo(outputEmc) < 0) {
			if (equation.output.size() == 1) {
				changeEmcMap(equation.output.get(0).getItem(), inputEmc);
				return;
			}
			warn("Recipe EMC conflict for recipe: " + getEquationName(equation)
				+ " recipe gives more EMC than you put in! Input: " + inputEmc + " Output: " + outputEmc
				+ ". please blacklist it or the other recipe(s) that gave these items value.");
			warn("Normally the items would be given new values to equalize the input and output, but the mapper"
				+ "cannot do that in this case because there is more than one output item.");
		}

	}


	// Should only be called to change a value that already exists.
	private void changeEmcMap(Item item, SuperNumber value) {
		emcMap.put(item, value);
	}

	private boolean putEmcMap(Item item, SuperNumber value, ItemEquation equation) {
		if (value.compareTo(SuperNumber.ZERO) <= 0) {
			warn("EMC Mapper tried assigning item " + itemName(item) 
				+ " a value lower or equal to 0. Current recipe: " + getEquationName(equation));
				return false;
		}
		if (!emcMapHasEntry(item)) {
			unlock(item);
			if (item == Items.HONEYCOMB)
				FabricatedExchange.LOGGER.info(value.divisionString());
			emcMap.put(item, value);
			return true;
		}
		SuperNumber emc = getItemEmc(item);
		if (emc.equals(value))
			return false;
		warn("Item EMC conflict for item " + itemName(item) 
			+ ", EMC Mapper tried assigning two different values! Original value: " + emc + ", new value: " + value
			+ ", current recipe: " + getEquationName(equation));
		return false;
	}


	private SuperNumber getItemEmc(Item item) {
		if (item == null)
			return SuperNumber.Zero(); 
		if (emcMap.containsKey(item)) 
			return new SuperNumber(emcMap.get(item));
		return SuperNumber.Zero(); 
	}
	private SuperNumber getItemStackEmc(ItemStack itemStack) {
		if (itemStack.isEmpty())
			return SuperNumber.Zero(); 
		Item item = itemStack.getItem();
		SuperNumber emc = getItemEmc(item);
		emc.multiply(itemStack.getCount());
		//considerStackDurability(itemStack, emc);
		//considerStackNbt(itemStack, emc);
		return emc;
	}




	private SuperNumber getIngredientEmc(Ingredient ingredient) {
		SuperNumber minEmc = SuperNumber.Zero();
		
		ItemStack[] arr = ingredient.getMatchingStacks();
		if (arr.length == 0)
			return minEmc;
		minEmc = getItemStackEmc(arr[0]);
		for (int i = 1; i < arr.length; i++) {
			SuperNumber emc = getItemStackEmc(arr[i]);
			minEmc = SuperNumber.min(minEmc, emc);
			if (emc.equalsZero())
				return SuperNumber.Zero();
		}
		return minEmc;
	}
	private int getIngredientCount(Ingredient ingredient) {
		// UNCONSIDERED: Ingredients with matching itemstacks of different counts
		for (ItemStack stack : ingredient.getMatchingStacks())
			return stack.getCount();
		return 1;
	}

	private void warn(String input) {
		warning.append(input).append('\n');
	}

	private void unlock(Item item) {
		if (!unknownEquationMap.containsKey(item))
			return;
		Set<MapperAction> set = unknownEquationMap.get(item);
		for (MapperAction action : set) {
			action.feed(item);
			if (!queuedActionsSet.contains(action) && action.shouldAdd())
				addActionToQueue(action);
		}
		unknownEquationMap.remove(item);
	}


	private boolean iteratePotionRecipe(BrewingRecipeAccessor<Potion> recipe) {
		boolean newInfo = false;

		Potion inputPotion = recipe.getInput();
		Ingredient ingredient = recipe.getIngredient();
		Potion outputPotion = recipe.getOutput();

		SuperNumber inputEmc = getPotionEmc(inputPotion);
		
		if (inputEmc.isNegative())
			return false;

		ItemStack[] stacks = ingredient.getMatchingStacks();
		SuperNumber ingredientEmc = SuperNumber.ZERO;
		for (ItemStack stack : stacks) {
			Item item = stack.getItem();
			ingredientEmc = getItemEmc(item);
			if (ingredientEmc.equalsZero())
				continue;
			for (ItemStack stack2 : stacks) {
				Item item2 = stack2.getItem();
				if (!item.equals(item2)) 
					newInfo = putEmcMap(item2, ingredientEmc, null) || newInfo;
			}
		}
		if (ingredientEmc.equalsZero())
			return false;
		
		ingredientEmc.divide(3);
		inputEmc.add(ingredientEmc);
		inputEmc.floor();

		putPotionEmcMap(outputPotion, inputEmc);


		return newInfo;
	}


	private boolean putPotionEmcMap(Potion potion, SuperNumber value) {
		if (!potionEmcMap.containsKey(potion)) {
			potionEmcMap.put(potion, value);
			return true;
		}
		return false;
	}

	// Potions, unlike items, return -1 when the map doesn't contain the potion
	private SuperNumber getPotionEmc(Potion potion) {
		if (potionEmcMap.containsKey(potion)) 
			return new SuperNumber(potionEmcMap.get(potion));
		return SuperNumber.NegativeOne(); 
	}




	/** This class represents an action the mapper is queued to do. */
	private interface MapperAction {
		/** When an item relevant to this action is given EMC, this action will be notified. */
		void feed(Item item);
		// Returns true if the action should be added to the queue
		boolean shouldAdd();
		void perform();
		int getLayer();
	}

	private class SolveEquationAction implements MapperAction {
		private int amountUnknown;
		private ItemEquation equation;
		private boolean isOutputUnknown;
		public SolveEquationAction(ItemEquation equation, int amountUnknown) {
			this.equation = equation;
			this.amountUnknown = amountUnknown;
		}


		private boolean isEquationOutputUnknown(ItemEquation equation) {
			for (ItemStack stack : equation.output) {
				if (!emcMapHasEntry(stack.getItem()))
					return true;
			}
			return false;
		}
		@Override
		public int getLayer() {
			int fromMinecraft = equation.origin.equals("minecraft") ? 0 : 1;
			int safe = isOutputUnknown ? 0 : 2;
			return fromMinecraft + safe;
		}
		@Override
		public void feed(Item item) {
			amountUnknown--;
			if (amountUnknown == 1)
				isOutputUnknown = isEquationOutputUnknown(equation);
		}
		@Override
		public boolean shouldAdd() {
			return amountUnknown <= 1;
		}

		@Override
		public void perform() {
			if (amountUnknown <= 0) {
				verify(equation);
				return;
			}
			solve(equation);
		}
	}


	private class EqualizeTagAction implements MapperAction {
		private List<Item> tagItems;
		boolean hasBeenFed;
		public EqualizeTagAction(List<Item> tagItems) {
			this.tagItems = tagItems;
			this.hasBeenFed = false;
		}

		@Override
		public void feed(Item item) {
			hasBeenFed = true;
		}
		@Override
		public boolean shouldAdd() {
			return hasBeenFed;
		}
		@Override
		public void perform() {
			equalizeList(tagItems);
		}
		@Override
		public int getLayer() {
			return 4;
		}
	}
	
	


}





