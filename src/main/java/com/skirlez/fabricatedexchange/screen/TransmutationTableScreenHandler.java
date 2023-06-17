package com.skirlez.fabricatedexchange.screen;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.skirlez.fabricatedexchange.FabricatedExchangeClient;
import com.skirlez.fabricatedexchange.emc.EmcData;
import com.skirlez.fabricatedexchange.screen.slot.transmutation.ConsumeSlot;
import com.skirlez.fabricatedexchange.screen.slot.transmutation.MidSlot;
import com.skirlez.fabricatedexchange.screen.slot.transmutation.TransmutationSlot;
import com.skirlez.fabricatedexchange.util.ModTags;
import com.skirlez.fabricatedexchange.util.PlayerState;
import com.skirlez.fabricatedexchange.util.ServerState;
import com.skirlez.fabricatedexchange.util.SuperNumber;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.collection.DefaultedList;

public class TransmutationTableScreenHandler extends ScreenHandler {
    private final Inventory inventory; // the transmutation slots
    private final Inventory minorInventory; // the middle slot and the slots on the left
    private final LivingEntity player;
    private ConsumeSlot emcSlot;
    private String searchText = "";
    private int offeringPageNum = 0;
    private List<Pair<Item, SuperNumber>> knowledge = new ArrayList<Pair<Item,SuperNumber>>();
    private final DefaultedList<Slot> transmutationSlots = DefaultedList.of();
    public TransmutationTableScreenHandler(int syncId, PlayerInventory inventory) {
        this(syncId, inventory, new SimpleInventory(18));
        
    }
    
    public TransmutationTableScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        super(ModScreenHandlers.TRANSMUTATION_TABLE_SCREEN_HANDLER, syncId);
        checkSize(inventory, 18);
        minorInventory = new SimpleInventory(1);
        this.inventory = inventory;
        this.player = playerInventory.player;
        inventory.onOpen(playerInventory.player);
        emcSlot = new ConsumeSlot(inventory, 0, 80, 64, player, this);
        this.addSlot(emcSlot);
        
        // use trigonometry to create the transmutation slots

        // outer ring
        double angle = 270.0;
        for (int i = 0; i < 12; i++) {
            double radianAngle = Math.toRadians(angle);
            int yOffset = (int)(Math.sin(radianAngle) * 41);
            int xOffset = (int)(Math.cos(radianAngle) * 41);
            addTransmutationSlot(new TransmutationSlot(inventory, i + 1, 131 + xOffset, 16 + yOffset, player, this));
            angle += 360.0 / 12.0;
        }

        // inner ring
        angle = 270.0;
        for (int i = 0; i < 4; i++) {
            double radianAngle = Math.toRadians(angle);
            int yOffset = (int)(Math.sin(radianAngle) * 19);
            int xOffset = (int)(Math.cos(radianAngle) * 19);
            addTransmutationSlot(new TransmutationSlot(inventory, i + 13, 131 + xOffset, 16 + yOffset, player, this));
            angle += 360.0 / 4.0;
        }

        addSlot(new MidSlot(minorInventory, 0, 131, 16, this, player.world.isClient));
        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);

        if (!player.getWorld().isClient()) {
            PlayerState playerState = ServerState.getPlayerState(player);
            for (int i = 0; i < playerState.knowledge.size(); i++) {
                String location = playerState.knowledge.get(i);
                String[] parts = location.split(":");
                Item item = Registries.ITEM.get(new Identifier(parts[0], parts[1]));
                SuperNumber emc = EmcData.getItemEmc(item);
                Pair<Item, SuperNumber> pair = new Pair<Item,SuperNumber>(item, emc);
                addKnowledgePair(pair);
            }
            refreshOffering();
        }
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }
    public void changeOfferingPage(int value) {
        offeringPageNum += value;
        refreshOffering();
    }

    public void refreshOffering() {
        SuperNumber emc = ServerState.getPlayerState(this.player).emc;
        SuperNumber midItemEmc = EmcData.getItemStackEmc(this.slots.get(17).getStack());
        if (!midItemEmc.equalsZero())
            emc = SuperNumber.min(emc, midItemEmc);
        LinkedList<Pair<Item, SuperNumber>> newKnowledge = new LinkedList<Pair<Item, SuperNumber>>(knowledge);
        LinkedList<Pair<Item, SuperNumber>> fuelKnowledge = new LinkedList<Pair<Item, SuperNumber>>();
        int len = newKnowledge.size();
        for (int i = 0; i < len; i++) {
            SuperNumber itemEmc = newKnowledge.get(i).getRight();
            if (emc.compareTo(itemEmc) == -1 || itemEmc.equalsZero()) {
                // emc filter - items who's emc value is greater than the players' emc shouldn't be displayed
                // (or if the item has 0 EMC which can happen if you learn it and then set the emc to 0)
                newKnowledge.remove(i); 
                i--;
                len--;
                continue;
            }
            Item item = newKnowledge.get(i).getLeft();
            if (Registries.ITEM.getEntry(item).streamTags().anyMatch(tag -> tag == ModTags.FUEL)) {
                newKnowledge.remove(i); // "fuel" items go in the inner ring, so we put them in this list
                i--;
                len--;
                fuelKnowledge.add(new Pair<Item, SuperNumber>(item, itemEmc));
            }
        }

        // make sure offering page is within bounds
        if (offeringPageNum != 0) {
            if (offeringPageNum * 12 >= newKnowledge.size())
                offeringPageNum--;
            else if (offeringPageNum < 0)
                offeringPageNum = 0;
        }

        // clear all the transmutation slots
        for (int i = 0; i < transmutationSlots.size(); i++) {
            transmutationSlots.get(i).setStack(ItemStack.EMPTY);
        }

        boolean isSearching = !searchText.isEmpty();
        int num = 0;
        for (int i = (isSearching) ? 0 : offeringPageNum * 12; i < newKnowledge.size(); i++) {
            Item item = newKnowledge.get(i).getLeft();

            String name = item.getName().getString();
            if (isSearching && !name.toLowerCase().contains(searchText.toLowerCase())) 
                continue; // search filter - items who don't have the search text as a substring shouldn't be displayed.
                // TODO: does this work for other languages?
            
            ItemStack stack = new ItemStack(item);
            transmutationSlots.get(num).setStack(stack);
            num++;
            if (num >= 12)
                break;
        }
        num = 0;
        for (int i = 0; i < fuelKnowledge.size(); i++) {
            Item item = fuelKnowledge.get(i).getLeft();

            String name = item.getName().getString();
            if (isSearching && !name.toLowerCase().contains(searchText.toLowerCase())) 
                continue; 
            
            ItemStack stack = new ItemStack(item);
            transmutationSlots.get(num + 12).setStack(stack);
            num++;
            if (num >= 4)
                return;
        }
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        dropInventory(player, minorInventory);
    }


    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        if (invSlot >= 1 && invSlot < 17) {
            
            ItemStack stack = ItemStack.EMPTY;
            TransmutationSlot slot = (TransmutationSlot)this.slots.get(invSlot);
            if (slot != null && slot.hasStack()) {
                stack = slot.getStack().copy();
                
                SuperNumber itemEmc = EmcData.getItemEmc(stack.getItem());
                if (itemEmc.equalsZero())
                    return ItemStack.EMPTY;

                SuperNumber emc;
                boolean client = player.getWorld().isClient();
                if (client)
                    emc = FabricatedExchangeClient.clientEmc;
                else
                    emc = EmcData.getEmc(player);
                
                SuperNumber itemCount = new SuperNumber(emc);
                itemCount.divide(itemEmc);
                itemCount.floor();
                
                SuperNumber sMaxCount = new SuperNumber(stack.getMaxCount());
                sMaxCount = SuperNumber.min(sMaxCount, itemCount);
                int a = sMaxCount.toInt();
                stack.setCount(a);

                SuperNumber itemCost = EmcData.getItemStackEmc(stack);

                if (emc.compareTo(itemCost) != -1) {
                    if (invSlot < this.inventory.size()) {
                        if (!this.insertItem(stack, this.inventory.size(), this.slots.size(), true)) 
                            return ItemStack.EMPTY;
                    } 
                    else if (!this.insertItem(stack, 0, this.inventory.size(), false))
                        return ItemStack.EMPTY;
                    
                    if (!client) {
                        EmcData.subtractEmc(player, itemCost);
                        refreshOffering();
                    }
                    return stack;
                }
                else
                    return ItemStack.EMPTY;
            }
            return stack;
        }
        // if it is not one of those slots it must mean we're shift clicking the inventory, meaning transmute this item
        Slot slot = this.slots.get(invSlot);
        ItemStack slotItemStack = slot.getStack();
        ItemStack itemStack = emcSlot.insertStack(slotItemStack, slotItemStack.getCount());
        if (!ItemStack.areEqual(slotItemStack, itemStack)) {
            slot.setStack(ItemStack.EMPTY);
        }
        
        return ItemStack.EMPTY; // we never actually want to move anything into the slot
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }


    public void addKnowledgePair(Pair<Item, SuperNumber> pair) {
        if (knowledge.size() == 0) {
            knowledge.add(pair);
            return;
        }

        SuperNumber num = pair.getRight();
        int low = 0;
        int high = knowledge.size() - 1;
        
        while (low <= high) {
            int mid = (low + high) / 2;
            Pair<Item, SuperNumber> midPair = knowledge.get(mid);
            SuperNumber midNum = midPair.getRight();
            
            if (num.compareTo(midNum) == 1) {
                high = mid - 1;
            } 
            else if (num.compareTo(midNum) == -1) {
                low = mid + 1;
            } 
            else {
                knowledge.add(mid + 1, pair);
                return;
            }
        }
        
        knowledge.add(low, pair);
    }


    private void addPlayerInventory(PlayerInventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(PlayerInventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    private void addTransmutationSlot(TransmutationSlot slot) {
        this.addSlot(slot);
        transmutationSlots.add(slot);
    }


    
}
