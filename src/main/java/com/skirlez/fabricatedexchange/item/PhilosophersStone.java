package com.skirlez.fabricatedexchange.item;

import java.util.Random;
import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.sound.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class PhilosophersStone extends Item {
    public PhilosophersStone(Settings settings) {
        super(settings);
    }
 
    public static final String CHARGE_KEY = "Charge";
    private final int maxCharge = 4;
    private Random r = new Random();

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getItemBarColor(ItemStack stack) {;
        return MathHelper.packRgb(0f, 0.7f, 1f);
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        int charge = stack.getOrCreateNbt().getInt(CHARGE_KEY);
        return Math.round((float)charge * 13.0f / (float)maxCharge);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        BlockPos blockPos = context.getBlockPos();
        World world = context.getWorld();
        Block block = world.getBlockState(blockPos).getBlock();
        PlayerEntity player = context.getPlayer();
        

        ItemStack stack = player.getStackInHand(context.getHand());
        NbtCompound stackNbt = stack.getOrCreateNbt();
        int charge = stackNbt.getInt(CHARGE_KEY);
        if (charge < maxCharge)
            stackNbt.putInt(CHARGE_KEY, charge + 1);
     
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
            }
        }

        return ActionResult.success(valid);
    }




}

