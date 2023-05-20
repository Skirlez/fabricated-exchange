package com.skirlez.fabricatedexchange.screen;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.screen.slot.ConsumeSlot;
import com.skirlez.fabricatedexchange.screen.slot.TransmutationSlot;
import com.skirlez.fabricatedexchange.util.PlayerState;
import com.skirlez.fabricatedexchange.util.ServerState;

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
import net.minecraft.util.collection.DefaultedList;

public class TransmutationTableScreenHandler extends ScreenHandler {
    private final Inventory inventory;
    private final LivingEntity player;
    private ConsumeSlot emcSlot;
    private Map<Item, BigInteger> knowledgeMap;

    private final DefaultedList<Slot> transmutationSlots = DefaultedList.of();
    public TransmutationTableScreenHandler(int syncId, PlayerInventory inventory) {
        this(syncId, inventory, new SimpleInventory(3));
        
    }

    public TransmutationTableScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        super(ModScreenHandlers.TRANSMUTATION_TABLE_SCREEN_HANDLER, syncId);
        checkSize(inventory, 2);
        this.inventory = inventory;
        this.player = playerInventory.player;
        inventory.onOpen(playerInventory.player);
        emcSlot = new ConsumeSlot(inventory, 0, 80, 66, player);
        this.addSlot(emcSlot);

        addTransmutationSlot(new TransmutationSlot(inventory, 1, 130, -22, player));
        addTransmutationSlot(new TransmutationSlot(inventory, 2, 160, -12, player));

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);

        if (!player.getWorld().isClient()) {
            Map<Item, BigInteger> unsortedKnowledgeMap = new HashMap<>();
            PlayerState playerState = ServerState.getPlayerState(player);
            for (int i = 0; i < playerState.knowledge.size(); i++) {
                String location = playerState.knowledge.get(i);
                String[] parts = location.split(":");
                Item item = Registries.ITEM.get(new Identifier(parts[0], parts[1]));
                BigInteger emc = FabricatedExchange.getItemEmc(item);
                unsortedKnowledgeMap.put(item, emc);
            }

            // hell (sort knowledgeMap based on the biginteger value)
            knowledgeMap = unsortedKnowledgeMap.entrySet().stream().sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

            refreshOffering();
        }
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        // since shift clicking means transmute, this method is different to a regular container
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

    private void addPlayerInventory(PlayerInventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 86 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(PlayerInventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 144));
        }
    }

    private void addTransmutationSlot(TransmutationSlot slot) {
        this.addSlot(slot);
        transmutationSlots.add(slot);
    }
    
    public void refreshOffering() {
        int num = 0;
        for (Item item : knowledgeMap.keySet()) {
            FabricatedExchange.LOGGER.info(String.valueOf(num));
            FabricatedExchange.LOGGER.info(item.getName().toString());
            transmutationSlots.get(num).setStack(new ItemStack(item));
            num++;
            if (num > 1)
                return;
        }
    }
}
