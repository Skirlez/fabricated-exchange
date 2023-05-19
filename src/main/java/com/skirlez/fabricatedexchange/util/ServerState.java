package com.skirlez.fabricatedexchange.util;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.UUID;

import com.skirlez.fabricatedexchange.FabricatedExchange;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
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
 
            playersNbtCompound.put(String.valueOf(UUID), playerStateNbt);
        });
        nbt.put("players", playersNbtCompound);
        nbt.putInt("balls", 33);
        FabricatedExchange.LOGGER.info("I DID THE THING");
        return nbt;
    }

 
    public static ServerState createFromNbt(NbtCompound tag) {
        ServerState serverState = new ServerState();
 
        NbtCompound playersTag = tag.getCompound("players");
        playersTag.getKeys().forEach(key -> {
            PlayerState playerState = new PlayerState(serverState);
 
            playerState.emc = new BigInteger(playersTag.getCompound(key).getString("emc"));
 
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