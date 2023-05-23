package com.skirlez.fabricatedexchange;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.impl.util.log.Log;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.recipe.SmeltingRecipe;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.skirlez.fabricatedexchange.block.ModBlockEntities;
import com.skirlez.fabricatedexchange.block.ModBlocks;
import com.skirlez.fabricatedexchange.item.ModItemGroups;
import com.skirlez.fabricatedexchange.item.ModItems;
import com.skirlez.fabricatedexchange.networking.ModMessages;
import com.skirlez.fabricatedexchange.screen.ModScreenHandlers;
import com.skirlez.fabricatedexchange.sound.ModSounds;
import com.skirlez.fabricatedexchange.util.EmcData;
import com.skirlez.fabricatedexchange.util.PlayerState;
import com.skirlez.fabricatedexchange.util.ServerState;
import com.skirlez.fabricatedexchange.util.SuperNumber;

public class FabricatedExchange implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("fabricated-exchange");
    public static final String MOD_ID = "fabricated-exchange";
    public static DecimalFormat formatter = new DecimalFormat("#,###.0000");
    // this map tells the philosopher's stone what block to transform when right clicked
    public static Map<Block, Block> blockRotationMap = new HashMap<>();
    public static Map<Item, SuperNumber> emcMap = new HashMap<>();

    @Override
    public void onInitialize() {
        ModItemGroups.registerItemGroups();
        ModItems.registerModItems();
        ModSounds.registerSoundEvents();
        ModBlocks.registerModBlocks();
        ModBlockEntities.registerBlockEntities();
        ModScreenHandlers.registerAllScreenHandlers();
        
        ModMessages.registerC2SPackets();
        fillBlockRotationMap();


        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> { 
            PlayerState playerState = ServerState.getPlayerState(handler.player);
            // Sending the packet to the player (look at the networking page for more information)
            EmcData.syncEmc((ServerPlayerEntity)handler.player, playerState.emc);
        });

        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            fillEmcMap(server.getOverworld());
        });
   
    }

    public static SuperNumber getItemEmc(Item item) {
        if (item == null)
            return SuperNumber.Zero(); 
        if (emcMap.containsKey(item)) {
            return new SuperNumber(emcMap.get(item));
        }
        return SuperNumber.Zero(); 
    }

    private void fillBlockRotationMap() {
        addTwoWayRelation(Blocks.GRASS_BLOCK, Blocks.SAND);
        addOneWayRelation(Blocks.DIRT, Blocks.SAND);
        addTwoWayRelation(Blocks.GRASS, Blocks.DEAD_BUSH);
        addChainRelation(new Block[] {Blocks.OAK_WOOD, Blocks.BIRCH_WOOD, Blocks.SPRUCE_WOOD});
        addChainRelation(new Block[] {Blocks.OAK_LOG, Blocks.BIRCH_LOG, Blocks.SPRUCE_LOG}); 
    }

    private void addTwoWayRelation(Block b1, Block b2) {
        blockRotationMap.put(b1, b2);
        blockRotationMap.put(b2, b1);
    };
    private void addOneWayRelation(Block b1, Block b2) {
        blockRotationMap.put(b1, b2);
    };
    private void addChainRelation(Block[] arr) {
        int i;
        for (i = 0; i < arr.length - 1; i++) {
            blockRotationMap.put(arr[i], arr[i + 1]);
        }
        blockRotationMap.put(arr[i], arr[0]);
    };


        
    private void fillEmcMap(World world) {
        /*
        This function will fill the emc map - the thing that sets the relation between items and their emc values.
        this implementation is dumb, because I am not experienced enough to implement a better solution.
        it loops over all the recipes in the game a LOT to generate the mappings.
        TODO: Save the result to make this less terrible.
        */
        emcMap.clear();


        // the seed values
        // TODO: read from json
        putEmcMap(Items.COBBLESTONE, 1);
        putEmcMap(Items.DIRT, 1);
        putEmcMap(Items.GRASS_BLOCK, 1);
        putEmcMap(Items.OAK_PLANKS,8);
        putEmcMap(Items.WHITE_WOOL, 48);
        putEmcMap(Items.BONE, 96);
        putEmcMap(Items.IRON_INGOT, 256);
        putEmcMap(Items.REDSTONE, 64);
        putEmcMap(Items.EMERALD, 16384);
        putEmcMap(Items.ENDER_PEARL, 1024);
        putEmcMap(Items.DIAMOND, 8192);
        putEmcMap(Items.NETHERITE_INGOT, 57344);


        RecipeManager recipeManager = world.getRecipeManager();
        // i don't know what this thing is, but you need it for some functions
        DynamicRegistryManager dynamicRegistryManager = world.getRegistryManager();


        for (int i = 0; i < 3; i++) {
            // Smelting recipes

            // Attempted goal is:
            // Item smelt = output item
            LinkedList<SmeltingRecipe> smeltingRecipes = new LinkedList<SmeltingRecipe>(recipeManager.listAllOfType(RecipeType.SMELTING));
            for (int j = 0; j < 100; j++) { 
                if (!iterateSmeltingRecipes(recipeManager, dynamicRegistryManager, smeltingRecipes))
                    break;
            }

            // Crafting recipes

            // Attempted goal is:
            // (Sum of all ingredients) = output


            LinkedList<CraftingRecipe> craftingRecipes = new LinkedList<CraftingRecipe>(recipeManager.listAllOfType(RecipeType.CRAFTING));
            for (int j = 0; j < 100; j++) {
                if (!iterateCraftingRecipes(recipeManager, dynamicRegistryManager, craftingRecipes))
                    break;
            }
        }

        
    }

    private boolean iterateCraftingRecipes(RecipeManager recipeManager, DynamicRegistryManager dynamicRegistryManager, LinkedList<CraftingRecipe> recipesList) { 
        boolean newInfo = false;
        
        int len = recipesList.size();
        for (int i = 0; i < len; i++) {
            CraftingRecipe recipe = recipesList.get(i);
            int unknownSide = 0; // 0 - unset, 1 - left, 2 - right
            List<Item> unknownItems = new ArrayList<Item>();
            boolean giveUp = false;
            List<Ingredient> ingredients = recipe.getIngredients();
            ItemStack outputStack = recipe.getOutput(dynamicRegistryManager);
            Item outputItem = outputStack.getItem();
            if (outputStack.getCount() == 0)
                continue; 

            SuperNumber outputEmc = getItemEmc(outputItem);
            outputEmc.multiply(outputStack.getCount());
            Pair<SuperNumber, SuperNumber> equation = new Pair<SuperNumber, SuperNumber>(SuperNumber.Zero(), SuperNumber.Zero());
          


            if (outputEmc.equalsZero()) {
                unknownSide = 2;
            }
            else {
                equation.getRight().add(outputEmc);
            }
            

            for (Ingredient ingredient : ingredients) {
                ItemStack[] itemStackArr = ingredient.getMatchingStacks();
                if (itemStackArr.length == 0)
                    continue;
                SuperNumber emcWorth = SuperNumber.Zero();

                for (ItemStack stack : itemStackArr) {

                    Item item = stack.getItem();
                    SuperNumber itemEmc = getItemEmc(item);
                    if (!itemEmc.equalsZero()) {
                        emcWorth = itemEmc;
                        break;
                    }
                }
                if (emcWorth.equalsZero()) {
                    if (unknownSide != 0) { // if there's already another unknown
                        giveUp = true;
                        break;
                    }
                    for (ItemStack stack : itemStackArr)
                        unknownItems.add(stack.getItem());
                    unknownSide = 1;
                }
                else {
                    for (ItemStack stack : itemStackArr) {
                        Item item = stack.getItem();
                        SuperNumber itemEmc = getItemEmc(item);
                        if (itemEmc.equals(SuperNumber.Zero()) && !emcMap.containsKey(item)) {
                            putEmcMap(item, emcWorth);
                            break;
                        }
                    }
                    equation.getLeft().add(emcWorth);
                }
                
            }
            if (giveUp)
                continue;

            

            if (unknownSide == 0) { // if we know the emc value of everything, remove this entry and move on
                recipesList.remove(i);
                i--;
                len--;
                continue;
            }
            else if (unknownSide == 1) {
                SuperNumber leftValue = equation.getLeft();
                SuperNumber rightValue = equation.getRight();
                rightValue.subtract(leftValue);
                if (rightValue.compareTo(SuperNumber.Zero()) == -1) {
                    LOGGER.error("ERROR: NEGATIVE EMC VALUE! Recipe: " + recipe.getId().toString());
                } 
                for (Item item : unknownItems) {
                    if (!emcMap.containsKey(item)) {
                        putEmcMap(item, rightValue);
                        newInfo = true;
                    }
                }
            }
            else {
                if (ItemStack.areItemsEqual(outputStack, new ItemStack(Items.DIAMOND_BLOCK))) {
                    FabricatedExchange.LOGGER.info(outputStack.getItem().getName().toString());
                    /* 
                    if (emcMap.containsKey(Items.DIAMOND_BLOCK))
                        LOGGER.info(emcMap.get(Items.DIAMOND_BLOCK).toString());
                        */
                }
                if (!emcMap.containsKey(outputItem)) {
                    SuperNumber result = equation.getLeft();
                    result.divide(outputStack.getCount());
                    putEmcMap(outputItem, result);
                    newInfo = true;
                }
            }
        }
        return newInfo;
    }


    private boolean iterateSmeltingRecipes(RecipeManager recipeManager, DynamicRegistryManager dynamicRegistryManager, LinkedList<SmeltingRecipe> recipesList) {
        boolean newInfo = false;

                
        int len = recipesList.size();
        for (int i = 0; i < len; i++) {
            SmeltingRecipe recipe = recipesList.get(i);

            // get the item (there should only be one, hopefully)
            Item inputItem = recipe.getIngredients().get(0).getMatchingStacks()[0].getItem();
            if (inputItem.equals(Items.OAK_WOOD)) {
                LOGGER.info("WOOOD");
            }

            Item outputItem = recipe.getOutput(dynamicRegistryManager).getItem();

            SuperNumber outEmc = getItemEmc(outputItem);
            SuperNumber inEmc = getItemEmc(inputItem);
            if (outEmc.equalsZero()) {
                if (inEmc.equalsZero())
                    continue; // not enough info

                // if in emc is defined but out emc is not
                if (!emcMap.containsKey(outputItem)) {
                    putEmcMap(outputItem, inEmc);
                    newInfo = true;
                }
            }
            else if (inEmc.equalsZero()) {
                 // if out emc is defined but in emc is not
                 if (!emcMap.containsKey(inputItem)) {
                    putEmcMap(inputItem, outEmc);
                    newInfo = true;
                 }
            }
            else { // we already know these EMC values
                recipesList.remove(i);
                i--;
                len--;
            }

            


        }
        return newInfo;
    }


    private void putEmcMap(Item item, int value) {
        emcMap.put(item, new SuperNumber(value));
    }

    private void putEmcMap(Item item, SuperNumber value) {
        emcMap.put(item, value);
    }


}