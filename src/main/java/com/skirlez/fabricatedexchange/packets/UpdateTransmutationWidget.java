package com.skirlez.fabricatedexchange.packets;

import com.skirlez.fabricatedexchange.screen.TransmutationTableScreenHandler;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;


public class UpdateTransmutationWidget extends ClientToServerPacket {
	public UpdateTransmutationWidget(String name) {
		super(name);
	}
	
	public static enum Type {
		UPDATE_SEARCH,
		UPDATE_PAGE,
	}
	public void sendPage(int page) {
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeEnumConstant(Type.UPDATE_PAGE);
		buf.writeInt(page);
		ClientPlayNetworking.send(id, buf);
	}
	public void sendSearch(String string) {
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeEnumConstant(Type.UPDATE_SEARCH);
		buf.writeString(string);
		ClientPlayNetworking.send(id, buf);
	}
	public void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler,
			PacketByteBuf buf, PacketSender responseSender) {   
		
		if (player.currentScreenHandler instanceof TransmutationTableScreenHandler screenHandler) {
			Type type = buf.readEnumConstant(Type.class);
			switch (type) {
				case UPDATE_SEARCH:
					screenHandler.setSearchText(buf.readString());
					screenHandler.refreshOffering();
					break;
				case UPDATE_PAGE:
					screenHandler.changeOfferingPage(buf.readInt()); 
				default:
					break;
			}
		}
	}	
}


