package com.skirlez.fabricatedexchange.screen;

import java.util.ArrayList;
import java.util.List;
import com.skirlez.fabricatedexchange.FabricatedExchangeClient;
import com.skirlez.fabricatedexchange.screen.slot.ConsumeSlot;
import com.skirlez.fabricatedexchange.screen.slot.MidSlot;
import com.skirlez.fabricatedexchange.screen.slot.TransmutationSlot;
import com.skirlez.fabricatedexchange.util.EmcData;
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
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.collection.DefaultedList;

public class TransmutationTableScreenHandler extends ScreenHandler {
    private final Inventory inventory;
    private final LivingEntity player;
    private ConsumeSlot emcSlot;
    private List<Pair<Item, SuperNumber>> knowledge = new ArrayList<Pair<Item,SuperNumber>>();
    private final DefaultedList<Slot> transmutationSlots = DefaultedList.of();
    public TransmutationTableScreenHandler(int syncId, PlayerInventory inventory) {
        this(syncId, inventory, new SimpleInventory(18));
        
    }

    public TransmutationTableScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        super(ModScreenHandlers.TRANSMUTATION_TABLE_SCREEN_HANDLER, syncId);
        checkSize(inventory, 18);
        this.inventory = inventory;
        this.player = playerInventory.player;
        inventory.onOpen(playerInventory.player);
        emcSlot = new ConsumeSlot(inventory, 0, 80, 66, player, this);
        this.addSlot(emcSlot);
        // use trigonometry to create the transmutation slots

        // outer ring
        double angle = 270.0;
        for (int i = 0; i < 12; i++) {
            double radianAngle = Math.toRadians(angle);
            int yOffset = (int)(Math.sin(radianAngle) * 41);
            int xOffset = (int)(Math.cos(radianAngle) * 41);
            addTransmutationSlot(new TransmutationSlot(inventory, i + 1, 131 + xOffset, 18 + yOffset, player, this));
            angle += 360.0 / 12.0;
        }

        // inner ring
        angle = 270.0;
        for (int i = 0; i < 4; i++) {
            double radianAngle = Math.toRadians(angle);
            int yOffset = (int)(Math.sin(radianAngle) * 19);
            int xOffset = (int)(Math.cos(radianAngle) * 19);
            addTransmutationSlot(new TransmutationSlot(inventory, i + 13, 131 + xOffset, 18 + yOffset, player, this));
            angle += 360.0 / 4.0;
        }


        addSlot(new MidSlot(inventory, 17, 131, 18, this, player.world.isClient));



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


    public void refreshOffering() {
        SuperNumber emc = ServerState.getPlayerState(this.player).emc;
        SuperNumber midItemEmc = EmcData.getItemStackEmc(this.slots.get(17).getStack());
        if (!midItemEmc.equalsZero())
            emc = SuperNumber.min(emc, midItemEmc);
        int num = 0;
        for (int i = 0; i < transmutationSlots.size(); i++) {
            transmutationSlots.get(i).setStack(ItemStack.EMPTY);
        }
        int len = Math.min(knowledge.size(), 12);
        for (int i = 0; i < len; i++) {
            Item item = knowledge.get(i).getLeft();
            SuperNumber itemEmc = knowledge.get(i).getRight();
            if (emc.compareTo(itemEmc) == -1)
                continue;
            
            ItemStack stack = new ItemStack(item);
            transmutationSlots.get(num).setStack(stack);
            num++;
            if (num >= transmutationSlots.size())
                return;
        }
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        super.onSlotClick(slotIndex, button, actionType, player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {

        if (invSlot >= 1 && invSlot < 17) {
            ItemStack stack = ItemStack.EMPTY;
            TransmutationSlot slot = (TransmutationSlot)this.slots.get(invSlot);
            if (slot != null && slot.hasStack()) {
                stack = slot.getStack().copy();
                
                SuperNumber emc;
                boolean client = player.getWorld().isClient();
                if (client)
                    emc = FabricatedExchangeClient.clientEmc;
                else
                    emc = EmcData.getEmc(player);
                

                
                SuperNumber itemCount = new SuperNumber(emc);
                itemCount.divide(EmcData.getItemEmc(stack.getItem()));
                itemCount.floor();

                int intItemCount = itemCount.toInt();
                if (intItemCount == 0)
                    stack.setCount(stack.getMaxCount());
                else
                    stack.setCount(Math.min(stack.getMaxCount(), intItemCount));

                SuperNumber itemCost = EmcData.getItemStackEmc(stack);


                if (emc.compareTo(itemCost) != -1) {
                    if (invSlot < this.inventory.size()) {
                        if (!this.insertItem(stack, this.inventory.size(), this.slots.size(), true)) {
                            return ItemStack.EMPTY;
                        }
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
    
}
