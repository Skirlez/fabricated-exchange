package com.skirlez.fabricatedexchange.block;

import com.google.common.collect.ImmutableSet;
import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.emc.EmcData;
import com.skirlez.fabricatedexchange.packets.ModServerToClientPackets;
import com.skirlez.fabricatedexchange.screen.EnergyCollectorScreen;
import com.skirlez.fabricatedexchange.screen.EnergyCollectorScreenHandler;
import com.skirlez.fabricatedexchange.screen.EnergyCollectorScreenHandler.SlotIndicies;
import com.skirlez.fabricatedexchange.util.GeneralUtil;
import com.skirlez.fabricatedexchange.util.ImplementedInventory;
import com.skirlez.fabricatedexchange.util.SingleStackInventoryImpl;
import com.skirlez.fabricatedexchange.util.config.ModDataFiles;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SingleStackInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;

public class EnergyCollectorBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, ImplementedInventory, ConsumerBlockEntity {
	
	public static final Set<Block> lightPassingBlocks = new ImmutableSet.Builder<Block>().add(
			ModBlocks.ENERGY_COLLECTOR_MK1, ModBlocks.ENERGY_COLLECTOR_MK2, ModBlocks.ENERGY_COLLECTOR_MK3,
			ModBlocks.ANTIMATTER_RELAY_MK1, ModBlocks.ANTIMATTER_RELAY_MK2, ModBlocks.ANTIMATTER_RELAY_MK3,
			ModBlocks.ENERGY_COLLECTOR_MK1, ModBlocks.ENERGY_CONDENSER_MK2).build();
	
	private long emc;
	private int tick;
	private int light;
	private boolean consuming;
	private final int level;

	private final long maximumEmc;
	private final double genPerTick;
	private final long outputRate;

	private double accum = 0;

	private final LinkedList<ServerPlayerEntity> players = new LinkedList<>();

	private final DefaultedList<ItemStack> stackContents;
	private final SingleStackInventory targetInventory;
	
	public EnergyCollectorBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.ENERGY_COLLECTOR, pos, state);
		emc = 0;
		this.targetInventory = new SingleStackInventoryImpl();
		tick = 0;
		Block block = state.getBlock();
		assert block instanceof EnergyCollector;
		this.level = ((EnergyCollector)block).getLevel();
		if (this.level == 0) {
			maximumEmc = 10000;
			genPerTick = 1d / 5d;
			outputRate = 10;
		}
		else if (this.level == 1) {
			maximumEmc = 30000;
			genPerTick = 3d / 5d;
			outputRate = 20;
		}
		else {
			maximumEmc = 60000;
			genPerTick = 2d;
			outputRate = 50;
		}

		this.stackContents = DefaultedList.ofSize(inventorySize(level), ItemStack.EMPTY);
	}

	@Override
	public Text getDisplayName() {
		return Text.translatable("screen.fabricated-exchange.emc_collection");
	}

	public static int getLightLevelAbove(World world, BlockPos pos) {
		if (ModDataFiles.MAIN_CONFIG_FILE.energyCollector_alwaysHaveEnergy)
			return 15;

		do {
			pos = pos.add(0, 1, 0);
		} while (lightPassingBlocks.contains(world.getBlockState(pos).getBlock()));

		return Math.min(world.getLightLevel(LightType.SKY, pos) - world.getAmbientDarkness(), 15);
	}
	
	public static void tick(World world, BlockPos blockPos, BlockState blockState, EnergyCollectorBlockEntity entity) {
		entity.light = getLightLevelAbove(world, blockPos);
		
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
		double addition = ((double)entity.light + 1d) / 16d;
		addition *= entity.genPerTick;
		entity.accum += addition;

		if (entity.accum >= 1) {
			long value = (long)entity.accum;
			entity.accum -= value;
			entity.emc += value;
			if (entity.emc > entity.maximumEmc)
				entity.emc = entity.maximumEmc;
		}
		if (entity.consuming == false) {
			entity.distributeEmc(GeneralUtil.getNeighboringBlockEntities(world, blockPos));
		}
		
		entity.serverSync(blockPos, entity.emc, entity.players);
		if (entity.tick % 120 == 0) 
			entity.markDirty();
		entity.tick++;
	}

	// returns true if the collector has a fuel item in the input slot and is able to move that item to the output slot
	private boolean tickInventoryLogic() {
		moveOutputToInput();
		moveInputToFuel();
		
		ItemStack fuelStack = this.getStack(SlotIndicies.FUEL_SLOT.ordinal());
		ItemStack outputStack = this.getStack(SlotIndicies.OUTPUT_SLOT.ordinal());
		
		if (fuelStack.isEmpty())
			return false;
		
		
		Item fuelItem = fuelStack.getItem();
		if (!FabricatedExchange.fuelProgressionMap.containsKey(fuelItem))
			return false;
		
		long fuelEmc = EmcData.getItemEmc(fuelItem).toLong(0);
		if (fuelEmc == 0)
			return false;
		long targetItemEmc = EmcData.getItemEmc(targetInventory.getStack().getItem()).toLong(0);
		if (targetItemEmc == 0)
			return false;
		if (!targetInventory.isEmpty()) {
			if (fuelEmc >= targetItemEmc)
				return false;
		}
		
		Item nextItem = FabricatedExchange.fuelProgressionMap.get(fuelItem);
		
		
		if ((!nextItem.equals(outputStack.getItem())
				|| outputStack.getMaxCount() <= outputStack.getCount()) && !outputStack.isEmpty())
			return false; // return because there's an item in the output slot that we cannot merge with the next item in the progression
		
		long nextEmc = EmcData.getItemEmc(nextItem).toLong(0);
		if (nextEmc == 0)
			return false;
		nextEmc -= fuelEmc;
		if (emc >= nextEmc) {
			if (outputStack.isEmpty()) {
				outputStack = new ItemStack(nextItem);
				this.setStack(SlotIndicies.OUTPUT_SLOT.ordinal(), outputStack);
			}
			else
				outputStack.increment(1);
			fuelStack.decrement(1);
			emc -= nextEmc;
		}
		moveOutputToInput();
		return true;
	}
	
	private void moveInputToFuel() {
		ItemStack fuelStack = this.getStack(SlotIndicies.FUEL_SLOT.ordinal());
	
		for (int i = SlotIndicies.INPUT_SLOTS_START.ordinal(); i < size(); i++) {
			ItemStack stack = getStack(i);
			if (stack.isEmpty())
				continue;
			if (fuelStack.isEmpty()) {
				this.setStack(SlotIndicies.FUEL_SLOT.ordinal(), stack);
				setStack(i, ItemStack.EMPTY);
				break;
			}
			if (ItemStack.canCombine(stack, fuelStack)) {
				int inputStackCount = stack.getCount();
				int max = stack.getMaxCount();
				if (inputStackCount + fuelStack.getCount() > max)
					inputStackCount = max - fuelStack.getCount();
				stack.decrement(inputStackCount);
				fuelStack.increment(inputStackCount);
				break;
			}	
		}
	}
	
	private void moveOutputToInput() {
		ItemStack outputStack = this.getStack(SlotIndicies.OUTPUT_SLOT.ordinal());
		for (int i = size() - 1; i >= SlotIndicies.INPUT_SLOTS_START.ordinal(); i--) {
			ItemStack stack = getStack(i);
			if (stack.isEmpty()) {
				setStack(i, outputStack);
				setStack(SlotIndicies.OUTPUT_SLOT.ordinal(), ItemStack.EMPTY);
				break;
			}
			
			else if (ItemStack.canCombine(stack, outputStack)) {
				int outputCount = outputStack.getCount();
				int max = stack.getMaxCount();
				if (outputCount + stack.getCount() > max)
					outputCount = max - stack.getCount();
				if (outputCount == 0)
					continue;
				outputStack.decrement(outputCount);
				stack.increment(outputCount);
				
				break;
			}	
		}	
	}
	
	public static int inventorySize(int level) {
		return 10 + level * 4;
	}
	
	@Override
	public boolean isValid(int slot, ItemStack stack) {
		return ((slot >= 2) || slot == 0) && FabricatedExchange.fuelProgressionMap.containsKey(stack.getItem());
	}
	@Override
	public boolean canTransferTo(Inventory hopperInventory, int slot, ItemStack stack) {
		return (slot == 1);
	}
	@Override
	public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
		ModServerToClientPackets.UPDATE_CONSUMER_BLOCK.send((ServerPlayerEntity)player, pos, emc);
		players.add((ServerPlayerEntity)player);
		return new EnergyCollectorScreenHandler(syncId, playerInventory, this, targetInventory, pos, level, Optional.empty());
	}

	@Override
	public DefaultedList<ItemStack> getItems() {
		return this.stackContents;
	}

	@Override
	protected void writeNbt(NbtCompound nbt) {
		super.writeNbt(nbt);
		Inventories.writeNbt(nbt, stackContents);
		nbt.putString("target", Registries.ITEM.getId(targetInventory.getStack(0).getItem()).toString());
		nbt.putString("emc", Long.toString(emc));
	
	}

	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);
		Inventories.readNbt(nbt, stackContents);
		Item item = Registries.ITEM.get(new Identifier(nbt.getString("target")));
		if (item == null)
			return;
		targetInventory.setStack(0, new ItemStack(item));
		emc = GeneralUtil.parseLongFromPossiblySuperNumberData(nbt.getString("emc"));
	}

	@Override
	public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
		// these will be read by the screen handler
		buf.writeBlockPos(pos);
		buf.writeInt(level);

		// these will only be read on the screen
		buf.writeString(Long.toString(emc));
		buf.writeInt(light);
	}
	
	@Override
	public boolean isConsuming() {
		return consuming;
	}
	@Override
	public long getEmc() {
		return emc;
	}
	public void setEmc(long emc) {
		this.emc = emc;
	}
	@Override
	public long getOutputRate() {
		return outputRate;
	}
	@Override
	public long getMaximumEmc() {
		return maximumEmc;
	}

	public void update(long emc) {
		this.emc = emc;
	}


	public int getLevel() {
		return this.level;
	}
}
