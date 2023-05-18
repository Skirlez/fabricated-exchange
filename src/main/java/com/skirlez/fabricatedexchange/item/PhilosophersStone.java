package com.skirlez.fabricatedexchange.item;

import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.sound.ModSounds;
import net.minecraft.block.Block;

import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PhilosophersStone extends Item {
     
    public PhilosophersStone(Settings settings) {
        

        super(settings);
    }
 
    
    


    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {


        BlockPos blockPos = context.getBlockPos();
        World world = context.getWorld();
        Block block = world.getBlockState(blockPos).getBlock();
        
        boolean valid = FabricatedExchange.blockRotationMap.containsKey(block);

        
        if (valid) {
            if (world.isClient())
                context.getPlayer().playSound(ModSounds.PS_USE, 1F, 1F);
            else
                world.setBlockState(blockPos, FabricatedExchange.blockRotationMap.get(block).getDefaultState());
        }

        return ActionResult.success(valid);
    }




}

