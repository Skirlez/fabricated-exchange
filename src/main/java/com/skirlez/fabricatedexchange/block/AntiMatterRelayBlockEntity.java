package com.skirlez.fabricatedexchange.block;

import java.util.LinkedList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.skirlez.fabricatedexchange.emc.EmcData;
import com.skirlez.fabricatedexchange.interfaces.ImplementedInventory;
import com.skirlez.fabricatedexchange.packets.ModServerToClientPackets;
import com.skirlez.fabricatedexchange.screen.AntiMatterRelayScreen;
import com.skirlez.fabricatedexchange.screen.AntiMatterRelayScreenHandler;
import com.skirlez.fabricatedexchange.screen.slot.FuelSlot;
import com.skirlez.fabricatedexchange.screen.slot.InputSlot;
import com.skirlez.fabricatedexchange.screen.slot.SlotCondition;
import com.skirlez.fabricatedexchange.util.GeneralUtil;
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
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class AntiMatterRelayBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, ImplementedInventory,
		ConsumerBlockEntity {

	private SuperNumber emc;
	private final SuperNumber outputRate;
	private final SuperNumber maximumEmc;
	private final SuperNumber bonusEmc;
	private final int level;
	private final DefaultedList<ItemStack> inventory;
	private final LinkedList<ServerPlayerEntity> players = new LinkedList<>();
	private int tick;

	private final DefaultedList<InputSlot> inputSlots = DefaultedList.of();
	private final FuelSlot fuelSlot;
	private final Slot chargeSlot;

	public AntiMatterRelayBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.ANTIMATTER_RELAY, pos, state);
		Block block = state.getBlock();
		if (block instanceof AntiMatterRelay)
			this.level = ((AntiMatterRelay)block).getLevel();
		else
			this.level = 0;
		
		int addition = (level == 0) ? 0 : (level == 1) ? 6 : 14;
		inventory = DefaultedList.ofSize(11 + addition, ItemStack.EMPTY);

		int xInput, yInput, xFuel, yFuel;
		if (level == 0) {
			outputRate = new SuperNumber(64);
			maximumEmc = new SuperNumber(100000);
			bonusEmc = new SuperNumber(1, 20);
			xInput = 0; yInput = 0; xFuel = 0; yFuel = 0;
		}
		else if (level == 1) {
			outputRate = new SuperNumber(192);
			maximumEmc = new SuperNumber(1000000);
			bonusEmc = new SuperNumber(3, 20);
			xInput = -1; yInput = 1; xFuel = 17; yFuel = 1;
		}
		else {
			outputRate = new SuperNumber(640);
			maximumEmc = new SuperNumber(10000000);
			bonusEmc = new SuperNumber(1, 2);
			xInput = 1; yInput = 1; xFuel = 37; yFuel = 15;
		}

		Inventory inv = (Inventory)this;
		fuelSlot = new FuelSlot(inv, 0, 67 + xFuel, 38 + yFuel, inputSlots, SlotCondition.alwaysTrue);
		chargeSlot = new Slot(inv, 1, 127 + xFuel, 38 + yFuel);
		for (int i = 0; i < 3 + level; i++) {
			for (int j = 0; j < 2 + level; j++)
				inputSlots.add(new InputSlot(inv, i * (2 + level) + j + 2, xInput + 27 + j * 18, yInput + 12 + i * 18, fuelSlot, SlotCondition.alwaysTrue));
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
		if (entity.fuelSlot.hasStack()) {
			ItemStack stack = entity.fuelSlot.getStack().copy();
			stack.setCount(1);
			SuperNumber value = EmcData.getItemStackEmc(stack);		  
			SuperNumber emcCopy = new SuperNumber(entity.emc);
			emcCopy.add(value);
			if (emcCopy.compareTo(entity.maximumEmc) != 1) {
				entity.emc.add(value);
				entity.fuelSlot.takeStack(1);
			}
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
	public boolean isValid(int slot, ItemStack stack) {
		return (slot > 1);
	}
	@Override
	public boolean canTransferTo(Inventory hopperInventory, int slot, ItemStack stack) {
		return false;
	}

	@Override
	public Text getDisplayName() {
		return Text.translatable("screen.fabricated-exchange.antimatter_relay");
	}

	@Nullable
	@Override
	public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
		ModServerToClientPackets.UPDATE_CONSUMER_BLOCK.send((ServerPlayerEntity)player, pos, emc);
		players.add((ServerPlayerEntity)player);
		return new AntiMatterRelayScreenHandler(syncId, inv, pos, level, null);
	}

	@Override
	public DefaultedList<ItemStack> getItems() {
		return this.inventory;
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
		Inventories.writeNbt(nbt, inventory);
		nbt.putString("antimatter_relay.emc", emc.divisionString());
	
	}
	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);
		Inventories.readNbt(nbt, inventory);
		emc = new SuperNumber(nbt.getString("antimatter_relay.emc"));
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


	public DefaultedList<InputSlot> getInputSlots() {
		return this.inputSlots;
	}
	public FuelSlot getFuelSlot() {
		return this.fuelSlot;
	}
	public Slot getChargeSlot() {
		return this.chargeSlot;
	}
}
