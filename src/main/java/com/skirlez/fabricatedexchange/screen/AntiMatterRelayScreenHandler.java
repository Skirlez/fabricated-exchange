package com.skirlez.fabricatedexchange.screen;

import com.skirlez.fabricatedexchange.block.AntiMatterRelayBlockEntity;
import com.skirlez.fabricatedexchange.util.GeneralUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;




public class AntiMatterRelayScreenHandler extends FuelScreenHandler {
    public AntiMatterRelayScreenHandler(int syncId, PlayerInventory inventory, PacketByteBuf buf) {
        this(syncId, inventory, 
        (AntiMatterRelayBlockEntity)inventory.player.getWorld().getBlockEntity(buf.readBlockPos()), 
        buf.readInt(), 
        buf);
    }
    public AntiMatterRelayScreenHandler(int syncId, PlayerInventory playerInventory, AntiMatterRelayBlockEntity blockEntity, int level, PacketByteBuf buf) {
        super(ModScreenHandlers.ANTIMATTER_RELAY_SCREEN_HANDLER, syncId, blockEntity, level, buf);

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
