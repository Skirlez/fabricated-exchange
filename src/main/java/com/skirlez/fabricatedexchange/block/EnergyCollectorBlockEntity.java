package com.skirlez.fabricatedexchange.block;

import org.jetbrains.annotations.Nullable;

import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.interfaces.ImplementedInventory;
import com.skirlez.fabricatedexchange.screen.EnergyCollectorScreenHandler;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EnergyCollectorBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, ImplementedInventory {
        private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(18, ItemStack.EMPTY);

    public EnergyCollectorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENERGY_COLLECTOR, pos, state);
    }

    public Text getDisplayName() {
        return Text.translatable("screen.fabricated-exchange.emc_collection");
    }


    public static void tick(World world, BlockPos blockPos, BlockState blockState, EnergyCollectorBlockEntity entity) {
        if (world.isClient())
            return;

    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new EnergyCollectorScreenHandler(syncId, inv, this);
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return this.inventory;
    }
}
