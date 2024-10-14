package com.skirlez.fabricatedexchange;

import com.google.common.collect.ImmutableMap;
import com.skirlez.fabricatedexchange.abilities.ItemAbilityManager;
import com.skirlez.fabricatedexchange.block.ModBlockEntities;
import com.skirlez.fabricatedexchange.block.ModBlocks;
import com.skirlez.fabricatedexchange.command.TheCommand;
import com.skirlez.fabricatedexchange.emc.EmcData;
import com.skirlez.fabricatedexchange.emc.EmcMapper;
import com.skirlez.fabricatedexchange.entities.ModEntities;
import com.skirlez.fabricatedexchange.item.ModItemGroups;
import com.skirlez.fabricatedexchange.item.ModItems;
import com.skirlez.fabricatedexchange.packets.ExtendedVanillaPackets;
import com.skirlez.fabricatedexchange.packets.ModClientToServerPackets;
import com.skirlez.fabricatedexchange.packets.ModServerToClientPackets;
import com.skirlez.fabricatedexchange.screen.ModScreenHandlers;
import com.skirlez.fabricatedexchange.sound.ModSoundEvents;
import com.skirlez.fabricatedexchange.util.ModTags;
import com.skirlez.fabricatedexchange.util.PlayerState;
import com.skirlez.fabricatedexchange.util.ServerState;
import com.skirlez.fabricatedexchange.util.SuperNumber;
import com.skirlez.fabricatedexchange.util.config.ModDataFiles;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public class FabricatedExchange implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("fabricated-exchange");
	public static final String MOD_ID = "fabricated-exchange";

	// this map tells the philosopher's stone what block to transform when right clicked
	
	// this map will be filled with all the items that have the transmutation_fuel tag, and each key value pair will represent a jump in progression between the fuel items
	// (e.g. in vanilla, coal (32 emc) will have a key value pair with redstone (64 emc), the next item with the fuel tag that has more emc than it.)
	public static Map<Item, Item> fuelProgressionMap = new HashMap<Item, Item>();

	// This set is different to the above map's keyset in that it also contains the very last item in the progression.
	public static Set<Item> fuelSet = new HashSet<Item>();
	
	public static final String VERSION = FabricLoader.getInstance().getModContainer(MOD_ID).get()
			.getMetadata().getVersion().getFriendlyString();
	
	@Override
	public void onInitialize() {
		ModItemGroups.register();
		ModItems.register();
		ModSoundEvents.register();
		ModBlocks.register();
		ModEntities.register();
		ModBlockEntities.register();
		ModScreenHandlers.register();
		ModClientToServerPackets.register();
		ExtendedVanillaPackets.register();
		ItemAbilityManager.registerServer();
		ModDataFiles.MAIN_CONFIG_FILE.fetch();
		
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			TheCommand.register(dispatcher);
		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> { 
			// send the player's own emc value
			ServerPlayerEntity player = handler.player;
			PlayerState playerState = ServerState.getPlayerState(player);
			ModServerToClientPackets.UPDATE_PLAYER_EMC.send(player, playerState.emc);
			ModServerToClientPackets.UPDATE_EMC_MAPS.send(player);
			ModServerToClientPackets.UPDATE_BLOCK_TRANSMUTATION_MAP.send(player);
			ModServerToClientPackets.UPDATE_NBT_ITEMS.send(player);
		});

		ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
			reload();
			calculateEmcMap(server);


		});


	}

	public static void reload() {
		ModDataFiles.fetchAll();
		BlockTransmutation.generateBlockRotationMap(ModDataFiles.BLOCK_TRANSMUTATION_MAP.getValue());
		
	}
	
	public static boolean calculateEmcMap(MinecraftServer server) {
		EmcMapper mapper = new EmcMapper(server.getOverworld().getRecipeManager(), server.getRegistryManager());
		boolean hasWarned = mapper.map();
		EmcData.emcMap = ImmutableMap.copyOf(mapper.getEmcMap());
		EmcData.potionEmcMap = ImmutableMap.copyOf(mapper.getPotionEmcMap());
		EmcData.enchantmentEmcMap = ImmutableMap.copyOf(mapper.getEnchantmentEmcMap());
		
		List<Item> fuelItemList = new ArrayList<Item>();
		Set<Item> newFuelSet = new HashSet<Item>();
		
		for (RegistryEntry<Item> entry : ModTags.FUEL_ITEMS.get()) { 
			Item item = entry.value();
			newFuelSet.add(item);
			int index = Collections.binarySearch(fuelItemList, item, 
				(item1, item2) -> 
					EmcData.getItemEmc(item1)
					.compareTo(
					EmcData.getItemEmc(item2)));

			if (index < 0)
				index = -index - 1;

			fuelItemList.add(index, item);
		}
		
		Map<Item, Item> newFuelProgressionMap = new HashMap<Item, Item>();
		for (int i = 0; i < fuelItemList.size(); i++) {
			Item item = fuelItemList.get(i);
			SuperNumber itemEmc = EmcData.getItemEmc(item);
			for (int j = i; j < fuelItemList.size(); j++) {
				Item nextItem = fuelItemList.get(j);
				SuperNumber nextEmc = EmcData.getItemEmc(nextItem);
				if (itemEmc.compareTo(nextEmc) == -1) { // if this item's emc is smaller than the next one
					newFuelProgressionMap.put(item, nextItem);
					break;
				}
			}
		}
		fuelProgressionMap = newFuelProgressionMap;
		fuelSet = newFuelSet;
		
		return hasWarned;
	}

	public static void syncMaps(MinecraftServer server) {
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList())
			ModServerToClientPackets.UPDATE_EMC_MAPS.send(player);
	}
	public static void syncBlockTransmutations(MinecraftServer server) {
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList())
			ModServerToClientPackets.UPDATE_BLOCK_TRANSMUTATION_MAP.send(player);
	}


}