package com.skirlez.fabricatedexchange.screen.slot.transmutation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.skirlez.fabricatedexchange.item.ModItems;
import com.skirlez.fabricatedexchange.item.NbtItem;
import com.skirlez.fabricatedexchange.screen.TransmutationTableScreenHandler;
import com.skirlez.fabricatedexchange.util.PlayerState;
import com.skirlez.fabricatedexchange.util.ServerState;
import com.skirlez.fabricatedexchange.util.config.ModDataFiles;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
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
                playerState.specialKnowledge = new ArrayList<NbtItem>();
                screenHandler.clearKnowledge();
            }
            else if (playerState.knowledge.contains(idName)) {
                playerState.knowledge.remove(idName);
                screenHandler.removeKnowledge(new NbtItem(item));
            }
            else if (ModDataFiles.NBT_ITEMS.hasItem(idName)) {
                NbtCompound nbt = stack.getNbt();
                List<String> allowedKeys = ModDataFiles.NBT_ITEMS.getAllowedKeys(idName);
                if (nbt == null)
                    nbt = new NbtCompound();
                if (!nbt.isEmpty()) {
                    Iterator<String> keyIterator = nbt.getKeys().iterator();
                    while (keyIterator.hasNext()) {
                        String key = keyIterator.next();
                        if (!allowedKeys.contains(key))
                            keyIterator.remove();
                    }
                }
            

                NbtItem nbtItem = new NbtItem(item, nbt);
                playerState.specialKnowledge.removeIf((currentNbtItem) -> (currentNbtItem.equalTo(nbtItem)));
                screenHandler.removeKnowledge(nbtItem);
            }
            playerState.markDirty();
            screenHandler.refreshOffering();    
        }

        super.setStack(stack);
    }



}
