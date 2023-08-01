package com.skirlez.fabricatedexchange.screen;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.FabricatedExchangeClient;
import com.skirlez.fabricatedexchange.emc.EmcData;
import com.skirlez.fabricatedexchange.networking.ModMessages;
import com.skirlez.fabricatedexchange.screen.slot.transmutation.ConsumeSlot;
import com.skirlez.fabricatedexchange.screen.slot.transmutation.ForgetSlot;
import com.skirlez.fabricatedexchange.screen.slot.transmutation.MidSlot;
import com.skirlez.fabricatedexchange.screen.slot.transmutation.TransmutationSlot;
import com.skirlez.fabricatedexchange.util.GeneralUtil;
import com.skirlez.fabricatedexchange.util.PlayerState;
import com.skirlez.fabricatedexchange.util.ServerState;
import com.skirlez.fabricatedexchange.util.SuperNumber;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

public class TransmutationTableScreenHandler extends ScreenHandler {
    private final Inventory inventory; // the transmutation slots
    private final PlayerEntity player;
    private final ConsumeSlot emcSlot;
    private String searchText = "";
    private int offeringPageNum = 0;
    private int lastOfferingPage = 0;
    private List<Item> orderedKnowledge = new ArrayList<Item>();
    private final DefaultedList<TransmutationSlot> transmutationSlots = DefaultedList.of();
    public TransmutationTableScreenHandler(int syncId, PlayerInventory inventory, PacketByteBuf buf) {
        this(syncId, inventory, new SimpleInventory(19));
        this.lastOfferingPage = buf.readInt();
    }
    
    public TransmutationTableScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        super(ModScreenHandlers.TRANSMUTATION_TABLE_SCREEN_HANDLER, syncId);
        checkSize(inventory, 19);
        this.inventory = inventory;
        this.player = playerInventory.player;
        inventory.onOpen(playerInventory.player);
        emcSlot = new ConsumeSlot(inventory, 2, 109, 97, player, this);
        addSlot(emcSlot);
        addSlot(new ForgetSlot(inventory, 1, 91, 97, player, this));
        addSlot(new MidSlot(inventory, 0, 159, 49, this, player.world.isClient));
        
        // use trigonometry to create the transmutation slots

        // outer ring
        double angle = 270.0;
        for (int i = 0; i < 12; i++) {
            addTransmutationSlot(new TransmutationSlot(inventory, i + 3, angle, player, this));
            angle += 360.0 / 12.0;
        }

        // inner ring
        angle = 270.0;
        for (int i = 0; i < 4; i++) {
            addTransmutationSlot(new TransmutationSlot(inventory, i + 15, angle, player, this));
            angle += 360.0 / 4.0;
        }



        
        // Inventory inventory, int index, int x, int y, PlayerEntity player,
       // TransmutationTableScreenHandler screenHandler
        
        GeneralUtil.addPlayerInventory(this, playerInventory, 37, 117);
        GeneralUtil.addPlayerHotbar(this, playerInventory, 37, 175);


        if (!player.getWorld().isClient()) {
            PlayerState playerState = ServerState.getPlayerState(player);
            Iterator<String> iterator = playerState.knowledge.iterator();
            while (iterator.hasNext()) {
                String location = iterator.next();
                String[] parts = location.split(":");
                Item item = Registries.ITEM.get(new Identifier(parts[0], parts[1]));
                addKnowledge(item);
            }
            refreshOffering();
        }
    }

    public void addKnowledge(Item item) {
        GeneralUtil.addSortedEmcList(orderedKnowledge, item, true);
    }
    public void removeKnowledge(Item item) {
        orderedKnowledge.remove(item);
    }
    public void clearKnowledge() {
        orderedKnowledge = new ArrayList<Item>();
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
        offeringPageNum = 0;
    }
    public void changeOfferingPage(int value) {
        offeringPageNum = value;
        refreshOffering();
    }

    public int getLastPageNum() {
        return lastOfferingPage;
    }

    public void setLastPageNum(int lastOfferingPage) {
        this.lastOfferingPage = lastOfferingPage;
    }

    public void refreshOffering() {
        SuperNumber emc = ServerState.getPlayerState(this.player).emc;
        SuperNumber midItemEmc = EmcData.getItemStackEmc(this.slots.get(0).getStack());
        if (!midItemEmc.equalsZero())
            emc = SuperNumber.min(emc, midItemEmc);
        
        List<Item> newKnowledge = new LinkedList<Item>();
        List<Item> fuelKnowledge = new LinkedList<Item>();
        boolean isSearching = !searchText.isEmpty();

        for (int i = 0; i < orderedKnowledge.size(); i++) {
            Item item = orderedKnowledge.get(i);
            SuperNumber itemEmc = EmcData.getItemEmc(item);
            // emc filter - items who's emc value is greater than the players' emc shouldn't be displayed
            // (or if the item has 0 EMC which can happen if you learn it and then set the emc to 0)
            if (emc.compareTo(itemEmc) == -1 || itemEmc.equalsZero()) {
                continue;            
            }

            // search filter - items who don't have the search text as a substring shouldn't be displayed.
            // TODO: does this work for other languages?
            
            String name = item.getName().getString();
            if (isSearching && !name.toLowerCase().contains(searchText.toLowerCase())) {
                continue; 
            }
            if (FabricatedExchange.fuelProgressionMap.containsKey(item)) {
                // fuel items go in the inner ring, so we put them in this list
                fuelKnowledge.add(item);
            }
            else
                newKnowledge.add(item);
        }
        lastOfferingPage = (newKnowledge.size() - 1) / 12;
        // make sure offering page is within bounds
        if (offeringPageNum != 0) {
            if (offeringPageNum > lastOfferingPage)
                offeringPageNum = lastOfferingPage;
            else if (offeringPageNum < 0)
                offeringPageNum = 0;
        }
        if (((ServerPlayerEntity)player).currentScreenHandler 
                instanceof TransmutationTableScreenHandler) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeInt(lastOfferingPage);
            ServerPlayNetworking.send((ServerPlayerEntity)player, ModMessages.TRANSMUTATION_TABLE_MAX_PAGE, buf);
        }

        // clear all the transmutation slots
        for (int i = 0; i < transmutationSlots.size(); i++) {
            transmutationSlots.get(i).setStack(ItemStack.EMPTY);
        }

        
        int num = 0;
        for (int i = offeringPageNum * 12; i < newKnowledge.size(); i++) {
            Item item = newKnowledge.get(i);
            ItemStack stack = new ItemStack(item);
            transmutationSlots.get(num).setStack(stack);
            num++;
            if (num >= 12)
                break;
        }
        num = 0;
        for (int i = 0; i < fuelKnowledge.size(); i++) {
            Item item = fuelKnowledge.get(i);
            String name = item.getName().getString();
            if (isSearching && !name.toLowerCase().contains(searchText.toLowerCase())) 
                continue; 
            
            ItemStack stack = new ItemStack(item);
            transmutationSlots.get(num + 12).setStack(stack);
            num++;
            if (num >= 4)
                break;
        }
        return;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
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
                        EmcData.subtractEmc((ServerPlayerEntity)player, itemCost);
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






    private void addTransmutationSlot(TransmutationSlot slot) {
        this.addSlot(slot);
        transmutationSlots.add(slot);
    }

    public DefaultedList<TransmutationSlot> getTransmutationSlots() {
        return transmutationSlots;
    }

}
