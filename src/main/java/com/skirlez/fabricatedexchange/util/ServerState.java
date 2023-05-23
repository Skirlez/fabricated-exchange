package com.skirlez.fabricatedexchange.util;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.UUID;

import com.skirlez.fabricatedexchange.FabricatedExchange;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

public class ServerState extends PersistentState {
    public HashMap<UUID, PlayerState> players = new HashMap<>();

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtCompound playersNbtCompound = new NbtCompound();
        players.forEach((UUID, playerState) -> {
            NbtCompound playerStateNbt = new NbtCompound();
 
            playerStateNbt.putString("emc", playerState.emc.toString());

            NbtList knowledgeList = new NbtList();
            for (int i = 0; i < playerState.knowledge.size(); i++) {
                knowledgeList.add(NbtString.of(playerState.knowledge.get(i)));
            }
            playerStateNbt.put("knowledge", knowledgeList);

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
 
            playerState.emc = new BigInteger(playersTag.getCompound(key).getString("emc"));
            NbtList knowledgeNbtList = playersTag.getCompound(key).getList("knowledge", NbtElement.STRING_TYPE);
            for (int i = 0; i < knowledgeNbtList.size(); i++) 
                playerState.knowledge.add(knowledgeNbtList.getString(i));
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