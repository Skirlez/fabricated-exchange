package com.skirlez.fabricatedexchange.block;

import java.util.LinkedList;

import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.emc.EmcData;
import com.skirlez.fabricatedexchange.interfaces.ImplementedInventory;
import com.skirlez.fabricatedexchange.packets.ModServerToClientPackets;
import com.skirlez.fabricatedexchange.screen.EnergyCollectorScreen;
import com.skirlez.fabricatedexchange.screen.EnergyCollectorScreenHandler;
import com.skirlez.fabricatedexchange.screen.slot.FakeSlot;
import com.skirlez.fabricatedexchange.screen.slot.FuelSlot;
import com.skirlez.fabricatedexchange.screen.slot.InputSlot;
import com.skirlez.fabricatedexchange.screen.slot.SlotCondition;
import com.skirlez.fabricatedexchange.screen.slot.collection.OutputSlot;
import com.skirlez.fabricatedexchange.util.GeneralUtil;
import com.skirlez.fabricatedexchange.util.SuperNumber;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

public class EnergyCollectorBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, ImplementedInventory,
		ConsumerBlockEntity {
	private final DefaultedList<ItemStack> inventory;
	private SuperNumber emc;
	private int tick;
	private int light;
	private boolean consuming;
	private final int level;

	private final SuperNumber maximumEmc;
	private final SuperNumber genPerTick;
	private final SuperNumber outputRate;
	private final LinkedList<ServerPlayerEntity> players = new LinkedList<>();

	private final DefaultedList<InputSlot> inputSlots = DefaultedList.of();
	private final FuelSlot fuelSlot;
	private final OutputSlot outputSlot;
	private final FakeSlot targetSlot;
	public EnergyCollectorBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.ENERGY_COLLECTOR, pos, state);
		emc = SuperNumber.Zero();
		tick = 0;
		Block block = state.getBlock();
		if (block instanceof EnergyCollector)
			this.level = ((EnergyCollector)block).getLevel();
		else
			this.level = 0;

		int xOffset;
		if (this.level == 0) {
			maximumEmc = new SuperNumber(10000);
			genPerTick = new SuperNumber(1, 5);
			outputRate = new SuperNumber(10);
			xOffset = 0;
		}
		else if (this.level == 1) {
			maximumEmc = new SuperNumber(30000);
			genPerTick = new SuperNumber(3, 5);
			outputRate = new SuperNumber(20);
			xOffset = 16;
		}
		else {
			maximumEmc = new SuperNumber(60000);
			genPerTick = new SuperNumber(2);
			outputRate = new SuperNumber(50);
			xOffset = 34;
		}
		int inputOffset = (level == 0) ? 38 : 36 + level * 18;
		inventory = DefaultedList.ofSize(11 + level * 4, ItemStack.EMPTY);

		Inventory inv = (Inventory)this;
		fuelSlot = new FuelSlot(inv, 0, xOffset + 124, 58, inputSlots, SlotCondition.isFuel);
		outputSlot = new OutputSlot(inv, 1, xOffset + 124, 13, inputSlots);
		targetSlot = new FakeSlot(inv, 2, xOffset + 153, 36);

		int ind = 3;
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 2 + level; j++) {
				inputSlots.add(new InputSlot(inv, ind++, inputOffset - j * 18, 62 - i * 18, fuelSlot, SlotCondition.isFuel));
			}
		}
	}


	@Override
	public Text getDisplayName() {
		return Text.translatable("screen.fabricated-exchange.emc_collection");
	}
	
	public static void tick(World world, BlockPos blockPos, BlockState blockState, EnergyCollectorBlockEntity entity) {
		entity.light = Math.max(world.getLightLevel(LightType.BLOCK, blockPos.add(0, 1, 0)),
			world.getLightLevel(LightType.SKY, blockPos.add(0, 1, 0)) - world.getAmbientDarkness());
		if (entity.light < 0)
			entity.light = 0;
		else if (entity.light > 15)
			entity.light = 15;
			
		if (world.isClient()) {
			MinecraftClient client = MinecraftClient.getInstance();
			if (client.player.currentScreenHandler instanceof EnergyCollectorScreenHandler screenHandler 
					&& screenHandler.getPos().equals(entity.pos) 
					&& client.currentScreen instanceof EnergyCollectorScreen screen)
				screen.update(entity.emc, entity.light);
			return;
		}

		entity.consuming = entity.tickInventoryLogic();
		SuperNumber addition = new SuperNumber(entity.light + 1, 16);

		addition.multiply(entity.genPerTick);
		entity.emc.add(addition);
		if (entity.emc.compareTo(entity.maximumEmc) == 1)
			entity.emc.copyValueOf(entity.maximumEmc);
		if (entity.consuming == false) {
			entity.distributeEmc(GeneralUtil.getNeighboringBlockEntities(world, blockPos));
		}
		
		entity.serverSync(blockPos, entity.emc, entity.players);
		if (entity.tick % 120 == 0) 
			entity.markDirty();
		entity.tick++;
	}

	// returns true if the collector has a fuel item in the fuel slot and is able to move that item to the output slot
	private boolean tickInventoryLogic() {
		ItemStack fuelStack = fuelSlot.getStack();
		if (fuelStack.isEmpty())
			return false;

		
		Item item = fuelStack.getItem();

		if (!FabricatedExchange.fuelProgressionMap.containsKey(item))
			return false;
		
		SuperNumber itemEmc = EmcData.getItemEmc(item);
		SuperNumber targetItemEmc = EmcData.getItemEmc(targetSlot.getStack().getItem());
		// check if we've gotten to the target item
		if (targetSlot.hasStack()) {
			if (itemEmc.compareTo(targetItemEmc) >= 0)
				return false;
		}
		Item nextItem = FabricatedExchange.fuelProgressionMap.get(item);
		

		if ((!nextItem.equals(outputSlot.getStack().getItem())
				|| outputSlot.getStack().getMaxCount() <= outputSlot.getStack().getCount()
				) && outputSlot.hasStack())
			return false; // return if there's an item in the output slot that we cannot merge with the next item in the progression
		

		SuperNumber nextEmc = EmcData.getItemEmc(nextItem);
		nextEmc.subtract(itemEmc);
		if (emc.compareTo(nextEmc) >= 0) {
			if (outputSlot.hasStack())
				outputSlot.getStack().increment(1);
			else
				outputSlot.setStack(new ItemStack(nextItem));

			// consider the target slot on whether we should move the output slot to the inputs
			if (targetSlot.hasStack()) {
				SuperNumber newItemEmc = EmcData.getItemEmc(outputSlot.getStack().getItem());
				if (newItemEmc.compareTo(targetItemEmc) < 0)
					outputSlot.moveToInputSlots();
			}
			else
				outputSlot.moveToInputSlots();
			fuelSlot.takeStack(1);
			emc.subtract(nextEmc);
		}
		return true;
	}

	@Override
	public boolean isValid(int slot, ItemStack stack) {
		return ((slot > 2) || slot == 0) && FabricatedExchange.fuelProgressionMap.containsKey(stack.getItem());
	}
	@Override
	public boolean canTransferTo(Inventory hopperInventory, int slot, ItemStack stack) {
		return (slot == 1);
	}


	@Override
	public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
		ModServerToClientPackets.UPDATE_CONSUMER_BLOCK.send((ServerPlayerEntity)player, pos, emc);
		players.add((ServerPlayerEntity)player);
		return new EnergyCollectorScreenHandler(syncId, inv, pos, level, null);
	}

	@Override
	public DefaultedList<ItemStack> getItems() {
		return this.inventory;
	}

	@Override
	protected void writeNbt(NbtCompound nbt) {
		super.writeNbt(nbt);
		Inventories.writeNbt(nbt, inventory);
		nbt.putString("energy_collector.emc", emc.divisionString());
	
	}

	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);
		Inventories.readNbt(nbt, inventory);
		emc = new SuperNumber(nbt.getString("energy_collector.emc"));
	}

	@Override
	public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
		// these will be read by the screen handler
		buf.writeBlockPos(pos);
		buf.writeInt(level);

		// these will only be read on the screen
		buf.writeString(emc.divisionString());
		buf.writeInt(light);
	}
	
	@Override
	public boolean isConsuming() {
		return consuming;
	}
	@Override
	public SuperNumber getEmc() {
		return emc;
	}
	@Override
	public SuperNumber getOutputRate() {
		return outputRate;
	}
	@Override
	public SuperNumber getMaximumEmc() {
		return maximumEmc;
	}

	public void update(SuperNumber emc) {
		this.emc = emc;
	}

	public FuelSlot getFuelSlot() {
		return fuelSlot;
	}
	public OutputSlot getOutputSlot() {
		return outputSlot;
	}
	public FakeSlot getTargetSlot() {
		return targetSlot;
	}
	public DefaultedList<InputSlot> getInputSlots() {
		return inputSlots;
	}

	public int getLevel() {
		return this.level;
	}
}
