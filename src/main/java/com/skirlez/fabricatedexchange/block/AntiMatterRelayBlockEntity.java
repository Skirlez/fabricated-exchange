package com.skirlez.fabricatedexchange.block;

import com.skirlez.fabricatedexchange.interfaces.ImplementedInventory;
import com.skirlez.fabricatedexchange.util.SuperNumber;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class AntiMatterRelayBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, ImplementedInventory,
        ConsumerBlockEntity {

    private SuperNumber emc;
    private SuperNumber outputRate;
    private final DefaultedList<ItemStack> inventory;
    public AntiMatterRelayBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ANTIMATTER_RELAY, pos, state);
        inventory = DefaultedList.ofSize(11, ItemStack.EMPTY);
    }
    public static void tick(World world, BlockPos blockPos, BlockState blockState, EnergyCollectorBlockEntity entity) {
    }


    @Override
    public Text getDisplayName() {
        return Text.translatable("screen.fabricated-exchange.antimatter_relay");
    }
    @Override
    public ScreenHandler createMenu(int var1, PlayerInventory var2, PlayerEntity var3) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createMenu'");
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
        return false;
    }
    @Override
    public DefaultedList<ItemStack> getItems() {
        return this.inventory;
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {

    }
}
