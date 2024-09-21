package com.skirlez.fabricatedexchange.packets;

import com.skirlez.fabricatedexchange.mixin.NetworkStateAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.NetworkState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;

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
		Map<NetworkSide, ? extends NetworkState.PacketHandler<?>> packetHandlersMap = ((NetworkStateAccessor) (Object) NetworkState.PLAY).getPacketHandlers();
		NetworkState.PacketHandler<ClientPlayPacketListener> packetHandler
			= (NetworkState.PacketHandler<ClientPlayPacketListener>) packetHandlersMap.get(NetworkSide.CLIENTBOUND);
		packetHandler.register(ExtraDataEntitySpawnS2CPacket.class, ExtraDataEntitySpawnS2CPacket::new);
	}
}
