package com.skirlez.fabricatedexchange.screen.slot;

import java.math.BigInteger;
import java.util.Optional;

import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.FabricatedExchangeClient;
import com.skirlez.fabricatedexchange.screen.TransmutationTableScreen;
import com.skirlez.fabricatedexchange.screen.TransmutationTableScreenHandler;
import com.skirlez.fabricatedexchange.util.EmcData;
import com.skirlez.fabricatedexchange.util.ModItemInterface;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class TransmutationSlot extends Slot {
    // This slot will contain an item the player has learned. taking it will subtract from the player's EMC.
    private LivingEntity player;
    private TransmutationTableScreenHandler screenHandler;
    public TransmutationSlot(Inventory inventory, int index, int x, int y, LivingEntity player, TransmutationTableScreenHandler screenHandler) {
        super(inventory, index, x, y);
        this.player = player;
        this.screenHandler = screenHandler;
        this.setStack(ItemStack.EMPTY);
    }

    @Override
    public boolean canTakeItems(PlayerEntity playerEntity) {
        return true;
    }

    @Override
    public ItemStack takeStack(int amount) {
        ItemStack stack = this.getStack().copy();
        stack.setCount(amount);
        ModItemInterface modStack = (ModItemInterface) (Object) stack;
        BigInteger itemCost = modStack.getEMC();
        if (!player.world.isClient()) {
            BigInteger emc = EmcData.getEmc(player);
            if (emc.compareTo(itemCost) == 1) {
                EmcData.addEmc(player, itemCost.negate());
                screenHandler.refreshOffering();
                return stack;
            }
            else
                return ItemStack.EMPTY;
        }
        if (FabricatedExchangeClient.clientEmc.compareTo(itemCost) == 1)
            return stack;
        else
            return ItemStack.EMPTY;
    }

    // you are never supposed to be able to put things here
    @Override
    public boolean canInsert(ItemStack stack) {
        return false;
    }

    // you're not supposed to be able to grab the item by double clicking on a stack of the same that isn't full
    @Override
    public ItemStack takeStackRange(int min, int max, PlayerEntity player) {
        return ItemStack.EMPTY;
    }

}
