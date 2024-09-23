package com.skirlez.fabricatedexchange.block;

import com.skirlez.fabricatedexchange.emc.EmcData;
import com.skirlez.fabricatedexchange.screen.AntiMatterRelayScreen;
import com.skirlez.fabricatedexchange.screen.AntiMatterRelayScreenHandler;
import com.skirlez.fabricatedexchange.screen.AntiMatterRelayScreenHandler.SlotIndicies;
import com.skirlez.fabricatedexchange.screen.slot.StackCondition;
import com.skirlez.fabricatedexchange.util.GeneralUtil;
import com.skirlez.fabricatedexchange.util.ImplementedInventory;
import com.skirlez.fabricatedexchange.util.config.ModDataFiles;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class AntiMatterRelayBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, ImplementedInventory,
		ConsumerBlockEntity {

	private long emc;
	private final long outputRate;
	private final long maximumEmc;
	private final double bonusEmcChance;
	
	private final int level;
	private final DefaultedList<ItemStack> stackContents;
	private final LinkedList<ServerPlayerEntity> players = new LinkedList<>();
	private int tick;


	public AntiMatterRelayBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.ANTIMATTER_RELAY, pos, state);
		Block block = state.getBlock();
		assert block instanceof AntiMatterRelay;
		this.level = ((AntiMatterRelay)block).getLevel();

		stackContents = DefaultedList.ofSize(inventorySize(level), ItemStack.EMPTY);

		if (level == 0) {
			outputRate = 64;
			maximumEmc = 100000;
			bonusEmcChance = 0.05d; //new SuperNumber(1, 20);
		}
		else if (level == 1) {
			outputRate = 192;
			maximumEmc = 1000000;
			bonusEmcChance = 0.15d; //new SuperNumber(3, 20);
		}
		else {
			outputRate = 640;
			maximumEmc = 10000000;
			bonusEmcChance = 0.5d; //new SuperNumber(1, 2);
		}

		emc = 0;
		tick = 0;
	}

	@Environment(EnvType.CLIENT)
	public static void clientTick(World world, BlockPos blockPos, BlockState blockState, AntiMatterRelayBlockEntity entity) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player.currentScreenHandler instanceof AntiMatterRelayScreenHandler screenHandler 
				&& screenHandler.getPos().equals(blockPos)
				&& client.currentScreen instanceof AntiMatterRelayScreen screen)
			screen.update(entity.emc);
		return;
	}


	public static void serverTick(World world, BlockPos blockPos, BlockState blockState, AntiMatterRelayBlockEntity entity) {
		Inventory inventory = (Inventory)entity;
		ItemStack fuelStack = inventory.getStack(SlotIndicies.FUEL_SLOT.ordinal());
		Integer burnTime = FuelRegistry.INSTANCE.get(fuelStack.getItem());

		if (burnTime != null || !ModDataFiles.MAIN_CONFIG_FILE.antiMatterRelay_onlyAcceptFuelItems) {
			if (!fuelStack.isEmpty()) {
				long value = EmcData.getItemStackEmc(fuelStack.copyWithCount(1)).toLong(-1);
				if (entity.emc <= entity.maximumEmc - value) {
					entity.emc += value;
					fuelStack.decrement(1);
				}
			}  
		}
		for (int i = 2; i < inventory.size(); i++) {
			ItemStack stack = inventory.getStack(i);
			if (stack.isEmpty())
				continue;
			if (fuelStack.isEmpty()) { 
				inventory.setStack(SlotIndicies.FUEL_SLOT.ordinal(), stack);
				inventory.setStack(i, ItemStack.EMPTY);
			}
			if (ItemStack.canCombine(fuelStack, stack)) {
				int remainder = fuelStack.getMaxCount() - fuelStack.getCount();
				int difference = remainder - stack.getCount();
				if (difference > remainder)
					difference = remainder;
				if (difference < 0)
					difference = 0;
				fuelStack.increment(remainder - difference);
				stack.decrement(remainder - difference);
			}	
			
			break;
		}
		
		if (entity.emc != 0) {
			List<BlockEntity> neighbors = GeneralUtil.getNeighboringBlockEntities(world, blockPos);

			List<BlockEntity> neighborsToDistribute = new ArrayList<BlockEntity>();

			for (BlockEntity blockEntity : neighbors) {
				if (!(blockEntity instanceof ConsumerBlockEntity) || blockEntity instanceof AntiMatterRelayBlockEntity)
					continue;
				if (((ConsumerBlockEntity)blockEntity).isConsuming()) {
					neighborsToDistribute.add(blockEntity);
				}
			}
			
			if (neighborsToDistribute.size() > 0) {
				entity.distributeEmc(neighborsToDistribute);
			}
		}

		entity.serverSync(blockPos, entity.emc, entity.players);
		if (entity.tick % 120 == 0) 
			entity.markDirty();
		entity.tick++;
	}

	@Override
	public boolean isValid(int invSlot, ItemStack stack) {
		return (invSlot == SlotIndicies.POWER_SLOT.ordinal() ? StackCondition.isBattery.passes(stack) : true);
	}
	@Override
	public boolean canTransferTo(Inventory hopperInventory, int invSlot, ItemStack stack) {
		return false;
	}

	public static int inventorySize(int level) {
		return ((level == 0) ? 8 : (level == 1) ? 14 : 22);
	}
	
	@Override
	public Text getDisplayName() {
		return Text.translatable("screen.fabricated-exchange.antimatter_relay");
	}

	@Nullable
	@Override
	public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
		//ModServerToClientPackets.UPDATE_CONSUMER_BLOCK.send((ServerPlayerEntity)player, pos, emc);
		players.add((ServerPlayerEntity)player);
		return new AntiMatterRelayScreenHandler(syncId, playerInventory, this, pos, level, Optional.empty());
	}

	@Override
	public DefaultedList<ItemStack> getItems() {
		return this.stackContents;
	}
	@Override
	public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
		// these will be read by the screen handler
		buf.writeBlockPos(pos);
		buf.writeInt(level);

		// these will only be read on the screen
		buf.writeString(Long.toString(emc));
	}
	
	@Override
	protected void writeNbt(NbtCompound nbt) {
		super.writeNbt(nbt);
		Inventories.writeNbt(nbt, stackContents);
		nbt.putString("emc", Long.toString(emc));
	
	}
	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);
		Inventories.readNbt(nbt, stackContents);

		emc = GeneralUtil.parseLongFromPossiblySuperNumberData(nbt.getString("emc"));
	}

	@Override
	public long getEmc() {
		return emc;
	}
	@Override
	public void setEmc(long emc) {
		this.emc = emc;
	}

	@Override
	public long getOutputRate() {
		return outputRate;
	}
	@Override
	public boolean isConsuming() {
		return true;
	}
	@Override
	public long getMaximumEmc() {
		return maximumEmc;
	}
	@Override
	public long getBonusEmc() {
		return (Math.random() <= bonusEmcChance) ? 1 : 0;
	}
	public void update(long emc) {
		this.emc = emc;
	}
}
