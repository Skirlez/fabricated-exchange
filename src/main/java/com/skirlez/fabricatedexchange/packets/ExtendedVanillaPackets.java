package com.skirlez.fabricatedexchange.packets;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.NetworkState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;

import java.lang.reflect.Field;
import java.util.Map;

public class ExtendedVanillaPackets {
	private ExtendedVanillaPackets () {}

	public static class ExtraDataEntitySpawnS2CPacket extends EntitySpawnS2CPacket
			implements Packet<ClientPlayPacketListener> {
		private NbtCompound extraData;
		public ExtraDataEntitySpawnS2CPacket(Entity entity, NbtCompound extraData) {
			super(entity);
			this.extraData = extraData;
		}
		public ExtraDataEntitySpawnS2CPacket(PacketByteBuf buf) {
			super(buf);
			extraData = buf.readNbt();
		}

		@Override
		public void write(PacketByteBuf buf) {
			super.write(buf);
			buf.writeNbt(extraData);
		}

		public NbtCompound getExtraData() {
			return extraData;
		}
	}

	@SuppressWarnings("unchecked")
	public static void register() {
		// I figured reflection would be better here than two separate mixins. I think it is, still.

		try {
			Field field = NetworkState.PLAY.getClass().getDeclaredField("packetHandlers");
			field.setAccessible(true);
			Map<NetworkSide, ? extends NetworkState.PacketHandler<?>> packetHandlersMap
				= (Map<NetworkSide, ? extends NetworkState.PacketHandler<ClientPlayPacketListener>>) field.get(NetworkState.PLAY);
			NetworkState.PacketHandler<ClientPlayPacketListener> packetHandler =
				(NetworkState.PacketHandler<ClientPlayPacketListener>) packetHandlersMap.get(NetworkSide.CLIENTBOUND);

			// Java doesn't like this, I'm not sure why.
			packetHandler.register(ExtraDataEntitySpawnS2CPacket.class, ExtraDataEntitySpawnS2CPacket::new);

			/*
			Function<PacketByteBuf, ExtraDataEntitySpawnS2CPacket> f = ExtraDataEntitySpawnS2CPacket::new;
			Method registerMethod = packetHandler.getClass().getMethod("register", Class.class, Function.class);
			registerMethod.invoke(packetHandler, ExtraDataEntitySpawnS2CPacket.class, f);
			*/

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
