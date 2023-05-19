package com.skirlez.fabricatedexchange.item;

import java.math.BigInteger;

import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.sound.ModSounds;
import com.skirlez.fabricatedexchange.util.EmcData;

import net.minecraft.block.Block;
import net.minecraft.entity.LivingEntity;
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
            else {
                world.setBlockState(blockPos, FabricatedExchange.blockRotationMap.get(block).getDefaultState());
                LivingEntity player = context.getPlayer();
                EmcData.setEmc(player, BigInteger.valueOf(3));
                
            }
        }

        return ActionResult.success(valid);
    }




}

