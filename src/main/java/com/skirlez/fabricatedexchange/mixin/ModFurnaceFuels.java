package com.skirlez.fabricatedexchange.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.skirlez.fabricatedexchange.block.ModBlocks;
import com.skirlez.fabricatedexchange.item.ModItems;

import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;

@Mixin(AbstractFurnaceBlockEntity.class)
public class ModFurnaceFuels {
    @Shadow
    private static void addFuel(Map<Item, Integer> fuelTimes, ItemConvertible item, int fuelTime) {
        
    }
    @Inject(method = "createFuelTimeMap", at = @At("RETURN"))
    private static void addModFuels(CallbackInfoReturnable<Map<Item, Integer>> cir) {
        Map<Item, Integer> mappa = cir.getReturnValue();
        // each one of these has x4 more burn time than the previous, with alchemical coal having 4x the burn time of coal
        // i would have ideally made it something like 1.5x for each jump but it would just mean the fuels are useless since coal blocks and a hopper are cheaper and burn longer
        addFuel(mappa, ModItems.ALCHEMICAL_COAL, 6400);
        addFuel(mappa, ModItems.RADIANT_COAL, 25600);
        addFuel(mappa, ModItems.MOBIUS_FUEL, 102400);
        addFuel(mappa, ModItems.AETERNALIS_FUEL, 409600);

        addFuel(mappa, ModBlocks.ALCHEMICAL_COAL_BLOCK, 64000);
        addFuel(mappa, ModBlocks.RADIANT_COAL_BLOCK, 256000);
        addFuel(mappa, ModBlocks.MOBIUS_FUEL_BLOCK, 1024000); 
        addFuel(mappa, ModBlocks.AETERNALIS_FUEL_BLOCK, 4096000); // 56.8888... hours in a regular furnace btw
    }
}
