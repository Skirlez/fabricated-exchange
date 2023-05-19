package com.skirlez.fabricatedexchange.screen.slot;

import java.math.BigInteger;

import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.util.EmcData;
import com.skirlez.fabricatedexchange.util.ModItemInterface;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;

public class ConsumeSlot extends Slot {
    private LivingEntity player;
    public ConsumeSlot(Inventory inventory, int index, int x, int y, LivingEntity player) {
        super(inventory, index, x, y);
        this.player = player;
    }

    @Override
    public ItemStack insertStack(ItemStack stack, int count) {
        ModItemInterface modStack = (ModItemInterface) (Object) stack;
        BigInteger emc = modStack.getEMC();
        if (emc.equals(BigInteger.ZERO))
            return stack;
        if (!player.getWorld().isClient())
            EmcData.addEmc(player, emc);
        return new ItemStack(Items.AIR);
    }

    @Override
    public ItemStack takeStackRange(int min, int max, PlayerEntity player) {
        FabricatedExchange.LOGGER.info("takeStackRange");
        return super.takeStackRange(min, max, player);
    }



    
}
