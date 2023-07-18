package com.skirlez.fabricatedexchange.block;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.skirlez.fabricatedexchange.emc.EmcData;
import com.skirlez.fabricatedexchange.interfaces.ImplementedInventory;
import com.skirlez.fabricatedexchange.networking.ModMessages;
import com.skirlez.fabricatedexchange.screen.AntiMatterRelayScreen;
import com.skirlez.fabricatedexchange.screen.AntiMatterRelayScreenHandler;
import com.skirlez.fabricatedexchange.screen.EnergyCollectorScreen;
import com.skirlez.fabricatedexchange.screen.EnergyCollectorScreenHandler;
import com.skirlez.fabricatedexchange.screen.slot.FuelSlot;
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
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
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
    private AntiMatterRelayScreenHandler handler;
    private final int level;
    private final DefaultedList<ItemStack> inventory;
    private int tick;
    public AntiMatterRelayBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ANTIMATTER_RELAY, pos, state);
        inventory = DefaultedList.ofSize(11, ItemStack.EMPTY);

        Block block = state.getBlock();
        if (block instanceof AntiMatterRelay)
            this.level = ((AntiMatterRelay)block).getLevel();
        else
            this.level = 0;

        outputRate = new SuperNumber(64);
        maximumEmc = new SuperNumber(100000);
        emc = SuperNumber.Zero();
        tick = 0;
    }

    public static void tick(World world, BlockPos blockPos, BlockState blockState, AntiMatterRelayBlockEntity entity) {
        boolean isClient = world.isClient();

        List<BlockEntity> neighbors = GeneralUtil.getNeighboringBlockEntities(world, blockPos);

        // the antimatter relay will only be consuming if no other surrounding blockentity is
        boolean hasConsumingNeighbors = false;
        for (BlockEntity blockEntity : neighbors) {
            if (!(blockEntity instanceof ConsumerBlockEntity))
                continue;
            if (((ConsumerBlockEntity)blockEntity).isConsuming()) {
                hasConsumingNeighbors = true;
                break;
            }
        }

        if (hasConsumingNeighbors)
            entity.distributeEmc(neighbors);
        
        if (isClient) {
            boolean success = entity.clientSync();
            if (!success)
                return;
            MinecraftClient client = MinecraftClient.getInstance();
            entity.handler = 
                (AntiMatterRelayScreenHandler)client.player.currentScreenHandler;
        }
        else { 
            if (entity.tick % 60 == 0)
                entity.serverSync();
            entity.tick++;
        }

        if (entity.handler == null)
            return;

        FuelSlot fuelSlot = entity.handler.getFuelSlot();
        if (!fuelSlot.hasStack())
            return;
        SuperNumber value = EmcData.getItemEmc(fuelSlot.getStack().getItem());
        if (!isClient)
            fuelSlot.takeStack(1);
        entity.emc.add(value);
    }


    private void serverSync() {
        PacketByteBuf data = PacketByteBufs.create();
        data.writeString(emc.divisionString());
        data.writeBlockPos(getPos());
        
        for(ServerPlayerEntity player : PlayerLookup.tracking((ServerWorld) world, getPos())) {
            ServerPlayNetworking.send(player, ModMessages.ANTIMATTER_RELAY_SYNC, data);
        }
    }
    private boolean clientSync() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player.currentScreenHandler instanceof AntiMatterRelayScreenHandler screenHandler
                && screenHandler.getBlockEntity().getPos().equals(pos)) {
            ((AntiMatterRelayScreen)client.currentScreen).update(emc);
            return true;
        }
        return false;
        
    }

    public void update(SuperNumber emc) {
        this.emc = emc;
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("screen.fabricated-exchange.antimatter_relay");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        handler = new AntiMatterRelayScreenHandler(syncId, inv, this, level);
        return handler;
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


}
