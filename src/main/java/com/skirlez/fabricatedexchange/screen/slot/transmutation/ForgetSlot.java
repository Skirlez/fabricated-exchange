package com.skirlez.fabricatedexchange.screen.slot.transmutation;

import java.util.HashSet;

import com.skirlez.fabricatedexchange.item.ModItems;
import com.skirlez.fabricatedexchange.screen.TransmutationTableScreenHandler;
import com.skirlez.fabricatedexchange.util.PlayerState;
import com.skirlez.fabricatedexchange.util.ServerState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.Slot;

public class ForgetSlot extends Slot {

    private final PlayerEntity player;
    private final TransmutationTableScreenHandler screenHandler;

    public ForgetSlot(Inventory inventory, int index, int x, int y, PlayerEntity player,
            TransmutationTableScreenHandler screenHandler) {
        super(inventory, index, x, y);
        this.player = player;
        this.screenHandler = screenHandler;
    }
    
    @Override
    public void setStack(ItemStack stack) {
        if (!player.getWorld().isClient() && !stack.isEmpty()) {
            PlayerState playerState = ServerState.getPlayerState(player);
            Item item = stack.getItem();
            String idName = Registries.ITEM.getId(item).toString();
            if (item.equals(ModItems.TOME_OF_KNOWLEDGE)) {
                playerState.knowledge = new HashSet<String>();
                screenHandler.clearKnowledge();
            }
            else if (playerState.knowledge.contains(idName)) {
                playerState.knowledge.remove(idName);
                screenHandler.removeKnowledge(item);
            }
            playerState.markDirty();
            screenHandler.refreshOffering();    
        }

        super.setStack(stack);
    }



}
