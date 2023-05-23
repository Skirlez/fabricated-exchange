package com.skirlez.fabricatedexchange.item;

import java.math.BigInteger;
import java.util.Random;

import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.sound.ModSounds;
import com.skirlez.fabricatedexchange.util.EmcData;

import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PhilosophersStone extends Item {
     
    public PhilosophersStone(Settings settings) {
        

        super(settings);
    }
 
    
    
    private Random r = new Random();

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        BlockPos blockPos = context.getBlockPos();
        World world = context.getWorld();
        Block block = world.getBlockState(blockPos).getBlock();
        
        boolean valid = FabricatedExchange.blockRotationMap.containsKey(block);

        
        if (valid) {
            if (world.isClient()) {
                context.getPlayer().playSound(ModSounds.PS_USE, 1F, 1F);
                for (int i = 0; i < 3; i++) {
                    world.addParticle(ParticleTypes.LARGE_SMOKE, 
                        (double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, 
                        (double)blockPos.getZ() + 0.5, r.nextDouble(0.2) - 0.1, 0.06, r.nextDouble(0.2) - 0.1);
                }
            }
            else {
                world.setBlockState(blockPos, FabricatedExchange.blockRotationMap.get(block).getDefaultState());
                LivingEntity player = context.getPlayer();
            }
        }

        return ActionResult.success(valid);
    }




}

