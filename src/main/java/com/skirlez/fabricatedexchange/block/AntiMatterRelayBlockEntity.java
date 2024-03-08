package com.skirlez.fabricatedexchange.block;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import net.fabricmc.fabric.api.registry.FuelRegistry;
import org.jetbrains.annotations.Nullable;

import com.skirlez.fabricatedexchange.emc.EmcData;
import com.skirlez.fabricatedexchange.screen.AntiMatterRelayScreen;
import com.skirlez.fabricatedexchange.screen.AntiMatterRelayScreenHandler;
import com.skirlez.fabricatedexchange.util.GeneralUtil;
import com.skirlez.fabricatedexchange.util.ImplementedInventory;
import com.skirlez.fabricatedexchange.util.SuperNumber;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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
import com.skirlez.fabricatedexchange.util.config.ModDataFiles;

import com.skirlez.fabricatedexchange.screen.AntiMatterRelayScreenHandler.SlotIndicies;
import com.skirlez.fabricatedexchange.screen.slot.StackCondition;

public class AntiMatterRelayBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, ImplementedInventory,
		ConsumerBlockEntity {

	private SuperNumber emc;
	private final SuperNumber outputRate;
	private final SuperNumber maximumEmc;
	private final SuperNumber bonusEmc;
	
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
			outputRate = new SuperNumber(64);
			maximumEmc = new SuperNumber(100000);
			bonusEmc = new SuperNumber(1, 20);
		}
		else if (level == 1) {
			outputRate = new SuperNumber(192);
			maximumEmc = new SuperNumber(1000000);
			bonusEmc = new SuperNumber(3, 20);
		}
		else {
			outputRate = new SuperNumber(640);
			maximumEmc = new SuperNumber(10000000);
			bonusEmc = new SuperNumber(1, 2);
		}

		emc = SuperNumber.Zero();
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
				SuperNumber value = EmcData.getItemStackEmc(fuelStack.copyWithCount(1));		  
				SuperNumber emcCopy = new SuperNumber(entity.emc);
				emcCopy.add(value);
				if (emcCopy.compareTo(entity.maximumEmc) != 1) {
					entity.emc.add(value);
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
		
		if (!entity.emc.equalsZero()) {
			List<BlockEntity> neighbors = GeneralUtil.getNeighboringBlockEntities(world, blockPos);
			boolean hasConsumingNeighbors = false;
			for (BlockEntity blockEntity : neighbors) {
				if (!(blockEntity instanceof ConsumerBlockEntity) || blockEntity instanceof AntiMatterRelayBlockEntity)
					continue;
				if (((ConsumerBlockEntity)blockEntity).isConsuming()) {
					hasConsumingNeighbors = true;
					break;
				}
			}
			
			if (hasConsumingNeighbors) {
				entity.distributeEmc(neighbors);
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
		buf.writeString(emc.divisionString());
	}
	
	@Override
	protected void writeNbt(NbtCompound nbt) {
		super.writeNbt(nbt);
		Inventories.writeNbt(nbt, stackContents);
		nbt.putString("emc", emc.divisionString());
	
	}
	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);
		Inventories.readNbt(nbt, stackContents);
		emc = new SuperNumber(nbt.getString("emc"));
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
	public boolean isConsuming() {
		return true;
	}
	@Override
	public SuperNumber getMaximumEmc() {
		return maximumEmc;
	}
	@Override
	public SuperNumber getBonusEmc() {
		return bonusEmc;
	}
	public void update(SuperNumber emc) {
		this.emc = emc;
	}
}
