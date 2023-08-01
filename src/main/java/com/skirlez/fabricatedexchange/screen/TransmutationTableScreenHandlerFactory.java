package com.skirlez.fabricatedexchange.screen;

import org.jetbrains.annotations.Nullable;

import com.skirlez.fabricatedexchange.interfaces.ImplementedInventory;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;

public class TransmutationTableScreenHandlerFactory implements ExtendedScreenHandlerFactory, ImplementedInventory {
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(19, ItemStack.EMPTY);
    private TransmutationTableScreenHandler handler;
    public TransmutationTableScreenHandlerFactory() {

    }
    public Text getDisplayName() {
        return Text.translatable("screen.fabricated-exchange.transmutation");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        handler = new TransmutationTableScreenHandler(syncId, inv, this);
        return handler;
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return this.inventory;
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeInt(handler == null ? 0 : handler.getLastPageNum());
    }
}
