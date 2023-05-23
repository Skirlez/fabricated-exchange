package com.skirlez.fabricatedexchange.screen.slot;


import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.screen.TransmutationTableScreenHandler;
import com.skirlez.fabricatedexchange.util.EmcData;
import com.skirlez.fabricatedexchange.util.ModItemInterface;
import com.skirlez.fabricatedexchange.util.PlayerState;
import com.skirlez.fabricatedexchange.util.ServerState;
import com.skirlez.fabricatedexchange.util.SuperNumber;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Pair;

public class ConsumeSlot extends Slot {
    // This slot destroys any item put inside and adds its EMC to it to a player.
    // if the item doesn't have EMC, it rejects it.

    private LivingEntity player;
    private TransmutationTableScreenHandler screenHandler;
    public ConsumeSlot(Inventory inventory, int index, int x, int y, LivingEntity player, TransmutationTableScreenHandler screenHandler) {
        super(inventory, index, x, y);
        this.player = player;
        this.screenHandler = screenHandler;
    }

    @Override
    public ItemStack insertStack(ItemStack stack, int count) {
        ItemStack newStack = stack.copy();
        newStack.setCount(count);
        ModItemInterface modStack = (ModItemInterface) (Object) newStack;
        SuperNumber emc = modStack.getEMC();
        if (emc.equalsZero())
            return stack;
        if (!player.getWorld().isClient()) {
            EmcData.addEmc(player, emc);
            PlayerState playerState = ServerState.getPlayerState(player);
            String idName = Registries.ITEM.getId(stack.getItem()).toString();
            Item item = stack.getItem();
            if (!playerState.knowledge.contains(idName)) {
                playerState.knowledge.add(idName);
                screenHandler.addKnowledgePair(new Pair<Item, SuperNumber>(item, FabricatedExchange.getItemEmc(item)));
            }
            playerState.markDirty();
            
            
            screenHandler.refreshOffering();    
        }

        // diff is the amount of the item the player had minus the amount they put in. if it's zero we give empty, otherwise we give what's left back.
        int diff = stack.getCount() - count;
        if (diff == 0) 
            return ItemStack.EMPTY; 
        stack.setCount(diff);
        return stack;
    }




    @Override
    public ItemStack takeStackRange(int min, int max, PlayerEntity player) {
        return super.takeStackRange(min, max, player);
    }

    // these two methods make sure that you can't actually insert anything into the slot by any means
    // well, inventory *is* public, so you could, but i would not like you
    @Override
    public void setStack(ItemStack stack) {
        
    }

    public boolean canInsert(ItemStack stack) {
        return false;
    }


    
}
