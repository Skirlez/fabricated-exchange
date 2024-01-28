package com.skirlez.fabricatedexchange.util;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.item.NbtItem;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

public class ServerState extends PersistentState {
	public Map<UUID, PlayerState> players = new HashMap<UUID, PlayerState>();

	@Override
	public NbtCompound writeNbt(NbtCompound nbt) {
		NbtCompound playersNbtCompound = new NbtCompound();
		players.forEach((UUID, playerState) -> {
			NbtCompound playerStateNbt = new NbtCompound();
			playerStateNbt.putString("emc", playerState.emc.divisionString());
			NbtList knowledgeList = new NbtList();

			for (Item item : playerState.knowledge)
				knowledgeList.add(NbtString.of(Registries.ITEM.getId(item).toString()));
			
			playerStateNbt.put("knowledge", knowledgeList);

			NbtList specialKnowledgeItemList = new NbtList();
			NbtList specialKnowledgeCompoundList = new NbtList();
			for (NbtItem item : playerState.specialKnowledge) {
				specialKnowledgeItemList.add(NbtString.of(Registries.ITEM.getId(item.asItem()).toString()));
				specialKnowledgeCompoundList.add(item.getNbt());
			}
			playerStateNbt.put("specialKnowledgeItems", specialKnowledgeItemList);
			playerStateNbt.put("specialKnowledgeCompounds", specialKnowledgeCompoundList);

			playersNbtCompound.put(String.valueOf(UUID), playerStateNbt);
		});
		nbt.put("players", playersNbtCompound);
		return nbt;
	}

 
	public static ServerState createFromNbt(NbtCompound tag) {
		ServerState serverState = new ServerState();
 
		NbtCompound playersTag = tag.getCompound("players");
		playersTag.getKeys().forEach(key -> {
			PlayerState playerState = new PlayerState(serverState);
 
			playerState.emc = new SuperNumber(playersTag.getCompound(key).getString("emc"));
			NbtList knowledgeNbtList = playersTag.getCompound(key).getList("knowledge", NbtElement.STRING_TYPE);
			
			for (int i = 0; i < knowledgeNbtList.size(); i++) {
				String id = knowledgeNbtList.getString(i);
				Item item = Registries.ITEM.get(new Identifier(id));
				if (item == null) {
					knowledgeNbtList.remove(i);
					i--;
					continue;
				}
				playerState.knowledge.add(item);
			}

			NbtList specialKnowledgeItemList = 
				playersTag.getCompound(key).getList("specialKnowledgeItems", NbtElement.STRING_TYPE);
			NbtList specialKnowledgeCompoundList = 
				playersTag.getCompound(key).getList("specialKnowledgeCompounds", NbtElement.COMPOUND_TYPE);
			
			for (int i = 0; i < specialKnowledgeItemList.size(); i++) {
				String id = specialKnowledgeItemList.getString(i);
				Item item = Registries.ITEM.get(new Identifier(id));
				if (item == null) {
					specialKnowledgeItemList.remove(i);
					i--;
					continue;
				}
				NbtCompound nbt = specialKnowledgeCompoundList.getCompound(i);
				NbtItem nbtItem = new NbtItem(Registries.ITEM.get(new Identifier(id)), nbt);
				playerState.specialKnowledge.add(nbtItem);
			}

			UUID uuid = UUID.fromString(key);
			serverState.players.put(uuid, playerState);
		});
 
		return serverState;
	}

	public static ServerState getServerState(MinecraftServer server) {
		PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();
 
		ServerState serverState = persistentStateManager.getOrCreate(
				ServerState::createFromNbt,
				ServerState::new,
				FabricatedExchange.MOD_ID);
 
		return serverState;
	}
 
	public static PlayerState getPlayerState(LivingEntity player) {
		ServerState serverState = getServerState(player.world.getServer());
		// Either get the player by the uuid, or we don't have data for him yet, make a new player state
		PlayerState playerState = serverState.players.computeIfAbsent(player.getUuid(), uuid -> new PlayerState(serverState));
 
		return playerState;
	}

}