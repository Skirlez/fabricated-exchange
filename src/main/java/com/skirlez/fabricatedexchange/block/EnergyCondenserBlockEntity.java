package com.skirlez.fabricatedexchange.block;

import java.util.LinkedList;
import java.util.Optional;

import com.skirlez.fabricatedexchange.emc.EmcData;
import com.skirlez.fabricatedexchange.screen.EnergyCondenserScreen;
import com.skirlez.fabricatedexchange.screen.EnergyCondenserScreenHandler;
import com.skirlez.fabricatedexchange.util.SingleStackInventoryImpl;
import com.skirlez.fabricatedexchange.util.SuperNumber;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SingleStackInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;



public class EnergyCondenserBlockEntity extends BaseChestBlockEntity implements ExtendedScreenHandlerFactory, ConsumerBlockEntity {
	private final int level;
	private SuperNumber emc;
	private int tick;
	
	private SingleStackInventory targetInventory;
	
	private final LinkedList<ServerPlayerEntity> players = new LinkedList<>();
	public EnergyCondenserBlockEntity(BlockPos pos, BlockState state) {
		this(ModBlockEntities.ENERGY_CONDENSER, pos, state);
		targetInventory = new SingleStackInventoryImpl();
	}

	public EnergyCondenserBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
		super(blockEntityType, pos, state);
		Block block = state.getBlock();
		emc = SuperNumber.Zero();
		if (block instanceof EnergyCondenser)
			this.level = ((EnergyCondenser)block).getLevel();
		else
			this.level = 0;
	}

	@Environment(EnvType.CLIENT)
	public void clientTick(World world, BlockPos blockPos, BlockState blockState) {
		progressAnimation();
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player.currentScreenHandler instanceof EnergyCondenserScreenHandler screenHandler 
				&& screenHandler.getPos().equals(blockPos)
				&& client.currentScreen instanceof EnergyCondenserScreen screen)
			screen.update(emc);

		return;
	}
	
	public void serverTick(World world, BlockPos blockPos, BlockState blockState) {
		Inventory inv = (Inventory)this;
		ItemStack target = targetInventory.getStack();
		SuperNumber targetEmc = EmcData.getItemStackEmc(target);
		if (!targetEmc.equalsZero() && emc.compareTo(targetEmc) >= 0) {
			int start = (level == 0) ? 0 : 42;
			SuperNumber emcCopy = new SuperNumber(emc);
			emcCopy.divide(targetEmc);
			int maxStacks = (level == 0) ? 1 : Math.min(emcCopy.toInt(target.getMaxCount()), target.getMaxCount());

			boolean success = false;
			for (int i = start; i < inv.size() && !success; i++) {
				ItemStack stack = inv.getStack(i);
				if (stack.isEmpty()) {
					inv.setStack(i, target.copyWithCount(maxStacks));
					targetEmc.multiply(maxStacks);
					success = true;
				}
				else if (ItemStack.canCombine(stack, target) && stack.getCount() < stack.getMaxCount()) {
					int increment = Math.min(stack.getMaxCount()-stack.getCount(), maxStacks);
					stack.increment(increment);
					targetEmc.multiply(increment);
					success = true;
				}
			}
			if (success) {   
				emc.subtract(targetEmc);
				inv.markDirty();
			}
		}
		if (level == 1 && emc.compareTo(targetEmc) == -1) {
			for (int i = 0; i < 42; i++) {
				ItemStack stack = inv.getStack(i);
				if (stack.isEmpty())
					continue;
				
				SuperNumber stackEmc = EmcData.getItemStackEmc(stack);
				if (stackEmc.equalsZero())
					continue;
				
				inv.removeStack(i);
				emc.add(stackEmc);
				break;
			}
		}


		serverSync(pos, emc, players);
		if (tick % 120 == 0) 
			markDirty();
		tick++;
	}

	@Override
	public boolean isValid(int invSlot, ItemStack stack) {
		return (level == 0) ? false : (invSlot < 43 && invSlot >= 0 && !EmcData.getItemStackEmc(stack).equalsZero());
	}
	@Override
	public boolean canTransferTo(Inventory hopperInventory, int invSlot, ItemStack stack) {
		return (level == 0) ? invSlot != 0 : invSlot > 42;
	}

	@Override
	public int size() {
		return inventorySize(this.level);
	}
	
	public static int inventorySize(int level) {
		return (13 - level) * 7;
	}

	@Override
	public ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
		ServerPlayerEntity player = (ServerPlayerEntity)playerInventory.player;
		players.add(player);
		return new EnergyCondenserScreenHandler(syncId, playerInventory, (Inventory)this, targetInventory, pos, level, Optional.empty());
	}

	public int getLevel() {
		return level;
	}

	@Override
	public SuperNumber getEmc() {
		return emc;
	}

	@Override
	public SuperNumber getOutputRate() {
		return SuperNumber.ZERO;
	}

	@Override
	public SuperNumber getMaximumEmc() {
		return EmcData.getItemStackEmc(targetInventory.getStack());
	}

	@Override
	public boolean isConsuming() {
		return !targetInventory.getStack().isEmpty();
	}

	@Override
	public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
		// intended to be read by the screen handler
		buf.writeBlockPos(pos);
		buf.writeInt(level);
		// intended to be read by the screen
		buf.writeString(emc.divisionString());
	}

	@Override
	public void update(SuperNumber emc) {
		this.emc = emc;
	}
	
	@Override
	public void writeNbt(NbtCompound tag) {
		super.writeNbt(tag);
		tag.putString("target", Registries.ITEM.getId(targetInventory.getStack(0).getItem()).toString());
	}
	
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);
		Item item = Registries.ITEM.get(new Identifier(nbt.getString("target")));
		if (item == null)
			return;
		targetInventory.setStack(0, new ItemStack(item));
	}
	


	public SingleStackInventory getTargetItemInventory() {
		return targetInventory;
	}

}