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
        // each one of these has x1.5 more burn time than the previous, with alchemical coal having 1.5x the burn time of coal
        addFuel(mappa, ModItems.ALCHEMICAL_COAL, 2400);
        addFuel(mappa, ModItems.RADIANT_COAL, 3600);
        addFuel(mappa, ModItems.MOBIUS_FUEL, 5400);
        addFuel(mappa, ModItems.AETERNALIS_FUEL, 8100);

        addFuel(mappa, ModBlocks.ALCHEMICAL_COAL_BLOCK, 24000);
        addFuel(mappa, ModBlocks.RADIANT_COAL_BLOCK, 36000);
        addFuel(mappa, ModBlocks.MOBIUS_FUEL_BLOCK, 54000);
        addFuel(mappa, ModBlocks.AETERNALIS_FUEL_BLOCK, 81000); // absurd
    }
}
