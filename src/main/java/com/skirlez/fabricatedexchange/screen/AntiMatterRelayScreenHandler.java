package com.skirlez.fabricatedexchange.screen;

import java.util.Optional;

import com.skirlez.fabricatedexchange.block.AntiMatterRelayBlockEntity;
import com.skirlez.fabricatedexchange.screen.slot.StackCondition;
import com.skirlez.fabricatedexchange.screen.slot.SlotWithCondition;
import com.skirlez.fabricatedexchange.util.GeneralUtil;
import com.skirlez.fabricatedexchange.util.config.ModDataFiles;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;

public class AntiMatterRelayScreenHandler extends FuelScreenHandler  {
	
	public static AntiMatterRelayScreenHandler clientConstructor(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
		BlockPos pos = buf.readBlockPos();
		int level = buf.readInt();
		return new AntiMatterRelayScreenHandler(syncId, playerInventory, 
				new SimpleInventory(AntiMatterRelayBlockEntity.inventorySize(level)),
				pos, level, Optional.of(buf));
	}
	
	public enum SlotIndicies {
		FUEL_SLOT,
		POWER_SLOT
	}
	
	public AntiMatterRelayScreenHandler(int syncId, PlayerInventory playerInventory, 
			Inventory inventory, BlockPos pos, int level, Optional<PacketByteBuf> buf) {
		super(ModScreenHandlers.ANTIMATTER_RELAY, syncId, inventory, pos, level, buf);

			
		int xInput, yInput, xFuel, yFuel, xInv, yInv;
		if (level == 0) {
			xInput = 0; 
			yInput = 0; 
			xFuel = 0; 
			yFuel = 0;
			xInv = 0; 
			yInv = 0;
		}
		else if (level == 1) {
			xInput = -1; 
			yInput = 1; 
			xFuel = 17; 
			yFuel = 1;
			xInv = 8; 
			yInv = 6;
		}
		else {
			xInput = 1; 
			yInput = 1; 
			xFuel = 37; 
			yFuel = 15;
			xInv = 18; 
			yInv = 18;
		}

		boolean onlyFuel = ModDataFiles.MAIN_CONFIG_FILE.antiMatterRelay_onlyAcceptFuelItems;
		StackCondition condition = (onlyFuel) ? StackCondition.isFuel : StackCondition.always;
		
		addSlot(new SlotWithCondition(inventory, SlotIndicies.FUEL_SLOT.ordinal(), 67 + xFuel, 38 + yFuel, condition));
		addSlot(new SlotWithCondition(inventory, SlotIndicies.POWER_SLOT.ordinal(), 127 + xFuel, 38 + yFuel, StackCondition.isBattery));
		
		int ind = 2;
		
		for (int i = 0; i < 3 + level; i++) {
			for (int j = 0; j < 2 + level; j++) {
				addSlot(new SlotWithCondition(inventory, ind, xInput + 27 + j * 18, yInput + 12 + i * 18, condition));
				ind++;
			}
		}
		
		
		GeneralUtil.addPlayerInventory(this, playerInventory, 8 + xInv, 90 + yInv);
		GeneralUtil.addPlayerHotbar(this, playerInventory, 8 + xInv, 148 + yInv);
	}


}
