package com.skirlez.fabricatedexchange.block;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.skirlez.fabricatedexchange.emc.EmcData;
import com.skirlez.fabricatedexchange.interfaces.ImplementedInventory;
import com.skirlez.fabricatedexchange.networking.ModMessages;
import com.skirlez.fabricatedexchange.screen.AntiMatterRelayScreen;
import com.skirlez.fabricatedexchange.screen.AntiMatterRelayScreenHandler;
import com.skirlez.fabricatedexchange.screen.slot.FuelSlot;
import com.skirlez.fabricatedexchange.screen.slot.InputSlot;
import com.skirlez.fabricatedexchange.screen.slot.SlotCondition;
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
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class AntiMatterRelayBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, ImplementedInventory,
        ConsumerBlockEntity {

    private SuperNumber emc;
    private final SuperNumber outputRate;
    private final SuperNumber maximumEmc;
    private final int level;
    private final DefaultedList<ItemStack> inventory;
    private int tick;

    private final DefaultedList<InputSlot> inputSlots = DefaultedList.of();
    private final FuelSlot fuelSlot;
    private final Slot chargeSlot;

    public AntiMatterRelayBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ANTIMATTER_RELAY, pos, state);
        inventory = DefaultedList.ofSize(11, ItemStack.EMPTY);

        Block block = state.getBlock();
        if (block instanceof AntiMatterRelay)
            this.level = ((AntiMatterRelay)block).getLevel();
        else
            this.level = 0;

        Inventory inv = (Inventory)this;
        fuelSlot = new FuelSlot(inv, 0, 67, 38, inputSlots, SlotCondition.alwaysTrue);
        chargeSlot = new Slot(inv, 1, 127, 38);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 2; j++)
                inputSlots.add(new InputSlot(inv, i * 2 + j + 2, 27 + j * 18, 12 + i * 18, fuelSlot, SlotCondition.alwaysTrue));
        }

        outputRate = new SuperNumber(64);
        maximumEmc = new SuperNumber(100000);
        emc = SuperNumber.Zero();
        tick = 0;
    }

    public static void tick(World world, BlockPos blockPos, BlockState blockState, AntiMatterRelayBlockEntity entity) {
        if (entity.fuelSlot.hasStack()) {

            SuperNumber value = EmcData.getItemEmc(entity.fuelSlot.getStack().getItem());                
            SuperNumber emcCopy = new SuperNumber(entity.emc);
            emcCopy.add(value);
            if (emcCopy.compareTo(entity.maximumEmc) != 1) {
                entity.emc.add(value);
                entity.fuelSlot.takeStack(1);
            }
        }   
    

        List<BlockEntity> neighbors = GeneralUtil.getNeighboringBlockEntities(world, blockPos);
        boolean hasConsumingNeighbors = false;
        for (BlockEntity blockEntity : neighbors) {
            if (!(blockEntity instanceof ConsumerBlockEntity))
                continue;
            if (((ConsumerBlockEntity)blockEntity).isConsuming()) {
                hasConsumingNeighbors = true;
                break;
            }
        }
        
        if (hasConsumingNeighbors) {
            entity.distributeEmc(neighbors);
        }

        if (world.isClient()) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player.currentScreenHandler instanceof AntiMatterRelayScreenHandler screenHandler
                    && screenHandler.getBlockEntity().getPos().equals(entity.pos)) {
                ((AntiMatterRelayScreen)client.currentScreen).update(entity.emc);
            }
        }
        else { 
            if (entity.tick % 60 == 0) {
                entity.serverSync();
                entity.markDirty();
            }
            entity.tick++;
        }

    }


    @Override
    public Text getDisplayName() {
        return Text.translatable("screen.fabricated-exchange.antimatter_relay");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        serverSyncPlayer((ServerPlayerEntity)player);
        return new AntiMatterRelayScreenHandler(syncId, inv, this, level, null);
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


    private void serverSync() {
        PacketByteBuf data = PacketByteBufs.create();
        data.writeString(emc.divisionString());
        data.writeBlockPos(getPos());
        
        for(ServerPlayerEntity player : PlayerLookup.tracking((ServerWorld) world, getPos())) {
            ServerPlayNetworking.send(player, ModMessages.ANTIMATTER_RELAY_SYNC, data);
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
