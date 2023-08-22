package com.skirlez.fabricatedexchange.screen.slot.transmutation;

import com.skirlez.fabricatedexchange.FabricatedExchangeClient;
import com.skirlez.fabricatedexchange.emc.EmcData;
import com.skirlez.fabricatedexchange.mixin.client.SlotModifier;
import com.skirlez.fabricatedexchange.screen.TransmutationTableScreenHandler;
import com.skirlez.fabricatedexchange.util.SuperNumber;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;

public class TransmutationSlot extends Slot {
    // This slot will contain an item the player has learned. taking it will subtract from the player's EMC.
    private PlayerEntity player;
    public double angle;
    public double distanceFromCenter = 0;
    public long startTime = System.currentTimeMillis();
    private TransmutationTableScreenHandler screenHandler;
    public TransmutationSlot(Inventory inventory, int index, double angle, PlayerEntity player, 
            TransmutationTableScreenHandler screenHandler) {
        super(inventory, index, 159, 49);
        this.player = player;
        this.screenHandler = screenHandler;
        this.angle = angle;
        
    }

    @Environment(EnvType.CLIENT)
    public void setPosition(int newX, int newY) {
        SlotModifier slot = (SlotModifier)this;
        slot.setX(newX);
        slot.setY(newY);
    }

    @Override
    public void setStack(ItemStack stack) {
        if (isEnabled())
            startTime = System.currentTimeMillis();
        super.setStack(stack);
    }

    @Override
    public boolean isEnabled() {
        return super.hasStack();
    }


    @Override
    public boolean canTakeItems(PlayerEntity playerEntity) {
        return true;
    }

    @Override
    public ItemStack takeStack(int amount) {
        ItemStack stack = this.getStack().copy();
        SuperNumber itemCost = EmcData.getItemStackEmc(stack);
        if (itemCost.equalsZero())
            return ItemStack.EMPTY;

        stack.setCount(amount);

        if (!player.world.isClient()) {
            SuperNumber emc = EmcData.getEmc(player);
            if (emc.compareTo(itemCost) != -1) {
                EmcData.subtractEmc((ServerPlayerEntity)player, itemCost);
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

    // you're not supposed to be able to grab the item by double clicking on a stack of the same item that isn't full
    @Override
    public ItemStack takeStackRange(int min, int max, PlayerEntity player) {
        return ItemStack.EMPTY;
    }

}
