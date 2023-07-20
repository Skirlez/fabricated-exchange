package com.skirlez.fabricatedexchange.block;

import org.jetbrains.annotations.Nullable;

import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.emc.EmcData;
import com.skirlez.fabricatedexchange.interfaces.ImplementedInventory;
import com.skirlez.fabricatedexchange.networking.ModMessages;
import com.skirlez.fabricatedexchange.screen.EnergyCollectorScreen;
import com.skirlez.fabricatedexchange.screen.EnergyCollectorScreenHandler;
import com.skirlez.fabricatedexchange.screen.slot.FakeSlot;
import com.skirlez.fabricatedexchange.screen.slot.FuelSlot;
import com.skirlez.fabricatedexchange.screen.slot.InputSlot;
import com.skirlez.fabricatedexchange.screen.slot.SlotCondition;
import com.skirlez.fabricatedexchange.screen.slot.collection.OutputSlot;
import com.skirlez.fabricatedexchange.util.GeneralUtil;
import com.skirlez.fabricatedexchange.util.SuperNumber;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
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
import net.minecraft.server.world.ServerWorld;
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
    private final SuperNumber emcMultiplier;
    private final SuperNumber outputRate;

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

        int inputOffset;
        int xOffset;
        if (this.level == 0) {
            maximumEmc = new SuperNumber(10000);
            emcMultiplier = new SuperNumber(1, 5);
            outputRate = new SuperNumber(10);
            inputOffset = 0;
            xOffset = 0;
        }
        else if (this.level == 1) {
            maximumEmc = new SuperNumber(30000);
            emcMultiplier = new SuperNumber(3, 5);
            outputRate = new SuperNumber(20);
            inputOffset = -2;
            xOffset = 16;
        }
        else {
            maximumEmc = new SuperNumber(60000);
            emcMultiplier = new SuperNumber(2);
            outputRate = new SuperNumber(50);
            inputOffset = -2;
            xOffset = 34;
        }
        inventory = DefaultedList.ofSize(11 + level * 4, ItemStack.EMPTY);

        Inventory inv = (Inventory)this;
        fuelSlot = new FuelSlot(inv, 0, xOffset + 124, 58, inputSlots, SlotCondition.isFuel);
        outputSlot = new OutputSlot(inv, 1, xOffset + 124, 13, inputSlots);
        targetSlot = new FakeSlot(inv, 2, xOffset + 153, 36);

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 2 + level; j++)
                inputSlots.add(new InputSlot(inv, i * (2 + level) + j + 3, inputOffset + 20 + j * 18, 8 + i * 18, fuelSlot, SlotCondition.isFuel));
        }
    }


    @Override
    public Text getDisplayName() {
        return Text.translatable("screen.fabricated-exchange.emc_collection");
    }
    
    public static void tick(World world, BlockPos blockPos, BlockState blockState, EnergyCollectorBlockEntity entity) {

        entity.light = world.getLightLevel(LightType.SKY, entity.getPos().add(0, 1, 0)) - world.getAmbientDarkness();
        if (entity.light < 0)
            entity.light = 0;

        if (world.isClient()) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player.currentScreenHandler instanceof EnergyCollectorScreenHandler screenHandler 
                    && screenHandler.getBlockEntity().getPos().equals(entity.pos))
                ((EnergyCollectorScreen)client.currentScreen).update(entity.emc, entity.light);
            return;
        }

        entity.consuming = entity.tickInventoryLogic();
        
        SuperNumber addition = new SuperNumber(entity.light, 15);
        addition.multiply(entity.emcMultiplier);
        entity.emc.add(addition);
        if (entity.emc.compareTo(entity.maximumEmc) == 1)
            entity.emc.copyValueOf(entity.maximumEmc);
        if (entity.consuming == false) {
            entity.distributeEmc(GeneralUtil.getNeighboringBlockEntities(world, blockPos));
        }
        
        entity.serverSync();
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
        // check if we've gotten to the target item
        if (targetSlot.hasStack()) {
            SuperNumber targetItemEmc = EmcData.getItemEmc(targetSlot.getStack().getItem());
            if (itemEmc.compareTo(targetItemEmc) >= 0)
                return false;
        }
        
        
        Item nextItem = FabricatedExchange.fuelProgressionMap.get(item);
        SuperNumber nextEmc = EmcData.getItemEmc(nextItem);

        if ((!nextItem.equals(outputSlot.getStack().getItem())
                || outputSlot.getStack().getMaxCount() <= outputSlot.getStack().getCount()
                ) && outputSlot.hasStack())
            return false; // return if there's an item in the output slot that we cannot merge with the next item in the progression
        
        nextEmc.subtract(itemEmc);
        if (emc.compareTo(nextEmc) >= 0) {
            if (outputSlot.hasStack())
                outputSlot.getStack().increment(1);
            else
                outputSlot.setStack(new ItemStack(nextItem));
            outputSlot.moveToInputSlots();
            fuelSlot.takeStack(1);
            emc.subtract(nextEmc);
        }
        return true;
    }



    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        serverSyncPlayer((ServerPlayerEntity)player);
        return new EnergyCollectorScreenHandler(syncId, inv, this, level, null);
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

    private void serverSync() {
        PacketByteBuf data = PacketByteBufs.create();
        data.writeBlockPos(getPos());
        data.writeString(emc.divisionString());
        
        for (ServerPlayerEntity player : PlayerLookup.tracking((ServerWorld) world, getPos())) {
            if (player.currentScreenHandler instanceof EnergyCollectorScreenHandler screenHandler && screenHandler.getBlockEntity().getPos().equals(pos)) 
                ServerPlayNetworking.send(player, ModMessages.ENERGY_COLLECTOR_SYNC, data);
        }
    }
    private void serverSyncPlayer(ServerPlayerEntity player) {
        PacketByteBuf data = PacketByteBufs.create();
        data.writeBlockPos(getPos());
        data.writeString(emc.divisionString());
 
        ServerPlayNetworking.send(player, ModMessages.ENERGY_COLLECTOR_SYNC, data);
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
}
