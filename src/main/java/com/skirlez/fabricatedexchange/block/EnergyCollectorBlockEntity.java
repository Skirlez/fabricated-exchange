package com.skirlez.fabricatedexchange.block;

import org.jetbrains.annotations.Nullable;

import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.emc.EmcData;
import com.skirlez.fabricatedexchange.interfaces.ImplementedInventory;
import com.skirlez.fabricatedexchange.networking.ModMessages;
import com.skirlez.fabricatedexchange.screen.EnergyCollectorScreenHandler;
import com.skirlez.fabricatedexchange.screen.slot.FakeSlot;
import com.skirlez.fabricatedexchange.screen.slot.collection.FuelSlot;
import com.skirlez.fabricatedexchange.screen.slot.collection.OutputSlot;
import com.skirlez.fabricatedexchange.util.SuperNumber;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

public class EnergyCollectorBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, ImplementedInventory {
    private final DefaultedList<ItemStack> inventory;
    private SuperNumber emc;
    private int tick;
    private int light;
    private final int level;
    private EnergyCollectorScreenHandler handler;
    private final SuperNumber maximumEmc;
    private final SuperNumber emcMultiplier;
    public EnergyCollectorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENERGY_COLLECTOR, pos, state);
        emc = SuperNumber.Zero();
        tick = 0;
        Block block = state.getBlock();
        if (block instanceof EnergyCollector)
            this.level = ((EnergyCollector)block).getLevel();
        else
            this.level = 0;

        if (this.level == 0) {
            maximumEmc = new SuperNumber(10000);
            emcMultiplier = new SuperNumber(1, 5);
        }
        else if (this.level == 1) {
            maximumEmc = new SuperNumber(30000);
            emcMultiplier = new SuperNumber(3, 5);
        }
        else {
            maximumEmc = new SuperNumber(60000);
            emcMultiplier = new SuperNumber(2);
        }
        inventory = DefaultedList.ofSize(11 + level * 4, ItemStack.EMPTY);
    }


    @Override
    public Text getDisplayName() {
        return Text.translatable("screen.fabricated-exchange.emc_collection");
    }

    public void update(SuperNumber emc, int light) {
        this.emc = emc;
        this.light = light;
    }

    private void sendSyncPacket() {
        PacketByteBuf data = PacketByteBufs.create();
        data.writeString(emc.divisionString());
        data.writeInt(light);
        data.writeBlockPos(getPos());

        for(ServerPlayerEntity player : PlayerLookup.tracking((ServerWorld) world, getPos())) {
            ServerPlayNetworking.send(player, ModMessages.ENERGY_COLLECTOR_SYNC, data);
        }
    }


    public static void tick(World world, BlockPos blockPos, BlockState blockState, EnergyCollectorBlockEntity entity) {
        if (world.isClient())
            return;
        if (entity.tick < 2) {
            entity.light = world.getLightLevel(LightType.SKY, entity.getPos().add(0, 1, 0)) - world.getAmbientDarkness();
            entity.sendSyncPacket();
        }
        
        SuperNumber addition = new SuperNumber(entity.light, 15);
        addition.multiply(entity.emcMultiplier);
        entity.emc.add(addition);
        if (entity.emc.compareTo(entity.maximumEmc) == 1)
            entity.emc.copyValueOf(entity.maximumEmc);
        entity.tick++;
        
        if (entity.tick % 60 == 0) {
            entity.light = world.getLightLevel(LightType.SKY, entity.getPos().add(0, 1, 0)) - world.getAmbientDarkness();
            entity.sendSyncPacket();
            entity.markDirty();
        }
        if (entity.handler == null)
            return;
        FuelSlot fuelSlot = (FuelSlot)entity.handler.getSlot(0);
        FakeSlot targetSlot = (FakeSlot)entity.handler.getSlot(entity.handler.getOutputSlotIndex() + 1);
        ItemStack stack = fuelSlot.getStack();
        if (stack.isEmpty())
            return;
        Item item = stack.getItem();
        if (!FabricatedExchange.fuelProgressionMap.containsKey(item))
            return;
        SuperNumber itemEmc = EmcData.getItemEmc(item);
        if (targetSlot.hasStack()) {
            SuperNumber targetItemEmc = EmcData.getItemEmc(targetSlot.getStack().getItem());
            if (itemEmc.compareTo(targetItemEmc) >= 0)
                return;
        }

        Item nextItem = FabricatedExchange.fuelProgressionMap.get(item);
        SuperNumber nextEmc = EmcData.getItemEmc(nextItem);
        OutputSlot outputSlot = (OutputSlot)entity.handler.getSlot(entity.handler.getOutputSlotIndex());

        if ((!nextItem.equals(outputSlot.getStack().getItem())
                || outputSlot.getStack().getMaxCount() <= outputSlot.getStack().getCount()
                ) && outputSlot.hasStack())
            return; // return if there's an item in the output slot that we cannot merge with the next item in the progression
        nextEmc.subtract(itemEmc);
        if (entity.emc.compareTo(nextEmc) >= 0) {
            if (outputSlot.hasStack())
                outputSlot.getStack().increment(1);
            else
                outputSlot.setStack(new ItemStack(nextItem));
            outputSlot.moveToInputSlots();
            fuelSlot.getStack().decrement(1);
            entity.handler.moveAllInputsToFuel();
            entity.emc.subtract(nextEmc);
            entity.sendSyncPacket();
        }

    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        tick = 0;
        handler = new EnergyCollectorScreenHandler(syncId, inv, this, level);
        return handler;
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
        Inventories.readNbt(nbt, inventory);
        super.readNbt(nbt);
        emc = new SuperNumber(nbt.getString("energy_collector.emc"));
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(level);
    }

}
