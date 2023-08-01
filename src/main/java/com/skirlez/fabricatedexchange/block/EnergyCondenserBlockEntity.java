package com.skirlez.fabricatedexchange.block;

import java.util.LinkedList;

import com.skirlez.fabricatedexchange.emc.EmcData;
import com.skirlez.fabricatedexchange.screen.EnergyCondenserScreen;
import com.skirlez.fabricatedexchange.screen.EnergyCondenserScreenHandler;
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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EnergyCondenserBlockEntity extends BaseChestBlockEntity implements ExtendedScreenHandlerFactory, ConsumerBlockEntity {
    private final int level;
    private SuperNumber emc;
    private int tick;
    private final LinkedList<ServerPlayerEntity> players = new LinkedList<>();
    public EnergyCondenserBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.ENERGY_CONDENSER, pos, state);
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
        Item target = inv.getStack(0).getItem();
        SuperNumber targetEmc = EmcData.getItemEmc(target);
        
        if (!targetEmc.equalsZero() && emc.compareTo(targetEmc) >= 0) {
            int start = (level == 0) ? 1 : 43;
            boolean success = false;
            for (int i = start; i < inv.size() && !success; i++) {
                ItemStack stack = inv.getStack(i);
                if (stack.isEmpty()) {
                    inv.setStack(i, new ItemStack(target));
                    success = true;
                }
                else if ((stack.getItem().equals(target) && stack.getCount() < stack.getMaxCount())) {
                    stack.increment(1);
                    success = true;
                }
            }
            if (success) {
                emc.subtract(targetEmc);
                inv.markDirty();
            }
        }
        if (level == 1 && emc.compareTo(targetEmc) == -1) {
            for (int i = 1; i < 43; i++) {
                ItemStack stack = inv.getStack(i);
                if (stack.isEmpty())
                    continue;
                
                SuperNumber itemEmc = EmcData.getItemEmc(stack.getItem());
                if (itemEmc.equalsZero())
                    continue;
                
                
                stack.decrement(1);
                emc.add(itemEmc);
                break;
            }
        }


        serverSync(pos, emc, players);
        if (tick % 120 == 0) 
            markDirty();
        tick++;
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return (level == 0) ? false : (slot < 43 && slot > 0 && !EmcData.getItemEmc(stack.getItem()).equalsZero());
    }
    @Override
    public boolean canTransferTo(Inventory hopperInventory, int slot, ItemStack stack) {
        return (level == 0) ? slot != 0 : slot > 42;
    }

    @Override
    public int size() {
        return (13 - level) * 7 + 1;
    }

    @Override
    public ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        ServerPlayerEntity player = (ServerPlayerEntity)playerInventory.player;
        serverSyncPlayer(pos, emc, player);
        players.add(player);
        return new EnergyCondenserScreenHandler(syncId, playerInventory, pos, level, null);
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
        return SuperNumber.ZERO;
    }

    @Override
    public boolean isConsuming() {
        return !getStack(0).isEmpty();
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

}