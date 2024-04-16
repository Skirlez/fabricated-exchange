package com.skirlez.fabricatedexchange;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;

import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class BlockTransmutation {
	public static ImmutableMap<Block, Block> blockTransmutationMap = ImmutableMap.of();
	
	public static boolean canTransmuteBlock(Block in) {
		return blockTransmutationMap.containsKey(in);
	}
	
	public static Optional<Block> getBlockToTransmute(Block in) {
		return Optional.ofNullable(blockTransmutationMap.get(in));
	}
	
	public static void generateBlockRotationMap(String[][] blockTransmutationData) {
		if (blockTransmutationData == null)
			return;
		
		Map<Block, Block> map = new HashMap<Block, Block>();
		for (int i = 0; i < blockTransmutationData.length; i++) {
			

			
			int len = blockTransmutationData[i].length;
			if (len < 2)
				continue;
			
			boolean isLastNull = blockTransmutationData[i][len - 1] == null;
			if (isLastNull) {
				for (int j = 0; j < len - 2; j++)
					addBlockRelation(blockTransmutationData[i][j], blockTransmutationData[i][j + 1], map);
				continue;
			}
			
			for (int j = 0; j < len - 1; j++) { 
				addBlockRelation(blockTransmutationData[i][j], blockTransmutationData[i][j + 1], map); 
			}
			addBlockRelation(blockTransmutationData[i][len - 1], blockTransmutationData[i][0], map); 
		}
		
		blockTransmutationMap = ImmutableMap.copyOf(map);
	}

	private static void addBlockRelation(String str1, String str2, Map<Block, Block> map) {
		Block b1 = Registry.BLOCK.get(new Identifier(str1));
		Block b2 = Registry.BLOCK.get(new Identifier(str2));
		if (b1 == null || b2 == null) {
			FabricatedExchange.LOGGER.error("Invalid block(s) found in block_transmutation_map.json! Block 1: " + str1 + " -> Block 2: " + str2);
			return;
		}
		if (map.containsKey(b1)) {
			FabricatedExchange.LOGGER.error("Duplicate block transmutation in block_transmutation_map.json! Block 1: " + str1 + " -> Block 2: " + str2);
			return;
		}
		map.put(b1, b2);
	};
	
}
