package com.skirlez.fabricatedexchange.screen;

import com.skirlez.fabricatedexchange.block.AntiMatterRelayBlockEntity;
import com.skirlez.fabricatedexchange.util.GeneralUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;




public class AntiMatterRelayScreenHandler extends FuelScreenHandler  {
    public AntiMatterRelayScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        this(syncId, playerInventory, buf.readBlockPos(), buf.readInt(), buf);
    }
    public AntiMatterRelayScreenHandler(int syncId, PlayerInventory playerInventory, BlockPos pos, int level, PacketByteBuf buf) {
        super(ModScreenHandlers.ANTIMATTER_RELAY_SCREEN_HANDLER, syncId, pos, level, buf);

        AntiMatterRelayBlockEntity blockEntity = (AntiMatterRelayBlockEntity)playerInventory.player.getWorld().getBlockEntity(pos);

        if (blockEntity == null) {
            inventory = new SimpleInventory(11 + ((level == 0) ? 0 : (level == 1) ? 6 : 14));
            return;
        }
        else
            inventory = (Inventory)blockEntity;

        addSlot(blockEntity.getFuelSlot());
        addSlot(blockEntity.getChargeSlot());
        inputSlots = blockEntity.getInputSlots();
        for (int i = 0; i < inputSlots.size(); i++) {
            addSlot(inputSlots.get(i));
        }
        int xInv, yInv;
        if (level == 0) {
            xInv = 0; 
            yInv = 0;
        }
        else if (level == 1) {
            xInv = 8; 
            yInv = 6;
        }
        else {
            xInv = 18; 
            yInv = 18;
        }
        GeneralUtil.addPlayerInventory(this, playerInventory, 8 + xInv, 90 + yInv);
        GeneralUtil.addPlayerHotbar(this, playerInventory, 8 + xInv, 148 + yInv);
    }


}
