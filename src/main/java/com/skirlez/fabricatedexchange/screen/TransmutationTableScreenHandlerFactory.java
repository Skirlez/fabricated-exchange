package com.skirlez.fabricatedexchange.screen;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class TransmutationTableScreenHandlerFactory implements ExtendedScreenHandlerFactory {
	private TransmutationTableScreenHandler handler;
	public TransmutationTableScreenHandlerFactory() {

	}
	public Text getDisplayName() {
		return Text.translatable("screen.fabricated-exchange.transmutation");
	}
	@Override
	public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
		handler = new TransmutationTableScreenHandler(syncId, inv);
		return handler;
	}
	@Override
	public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
		buf.writeInt(handler == null ? 0 : handler.getLastPageNum());
	}
}
