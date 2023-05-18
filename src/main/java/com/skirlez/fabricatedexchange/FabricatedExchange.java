package com.skirlez.fabricatedexchange;

import net.fabricmc.api.ModInitializer;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.skirlez.fabricatedexchange.block.ModBlockEntities;
import com.skirlez.fabricatedexchange.block.ModBlocks;
import com.skirlez.fabricatedexchange.item.ModItemGroups;
import com.skirlez.fabricatedexchange.item.ModItems;
import com.skirlez.fabricatedexchange.screen.ModScreenHandlers;
import com.skirlez.fabricatedexchange.sound.ModSounds;

public class FabricatedExchange implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("fabricated-exchange");
    public static final String MOD_ID = "fabricated-exchange";
    
    // this map tells the philosopher's stone what block to transform when right clicked
    public static Map<Block, Block> blockRotationMap = new HashMap<>();
    public static Map<Item, BigInteger> emcMap = new HashMap<>();

    @Override
    public void onInitialize() {
        emcMap.put(Items.STONE, BigInteger.valueOf(1));
        ModItemGroups.registerItemGroups();
        ModItems.registerModItems();
        ModSounds.registerSoundEvents();
        ModBlocks.registerModBlocks();
        ModBlockEntities.registerBlockEntities();
        ModScreenHandlers.registerAllScreenHandlers();
        fillBlockRotationMap();
    }

    public static BigInteger getItemEmc(Item item) {
        if (item == null)
            return BigInteger.ZERO; 
        LOGGER.info(item.toString());
        if (emcMap.containsKey(item)) {
            
            return emcMap.get(item);
        }
        return BigInteger.ZERO; 
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

    
    

}