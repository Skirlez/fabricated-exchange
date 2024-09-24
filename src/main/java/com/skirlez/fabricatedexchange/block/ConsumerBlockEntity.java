package com.skirlez.fabricatedexchange.block;

import com.skirlez.fabricatedexchange.packets.ModServerToClientPackets;
import com.skirlez.fabricatedexchange.screen.LeveledScreenHandler;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** This interface is used by block entities that can share and take EMC from other block entities. 
the block entites can be in one of two states: an idle state and a consuming state.
For example: When the Energy Collectors have no fuel items in them, They are in the idle state,
simply generating EMC. When a fuel item is put inside of them, they are in the consuming state,
consuming their EMC in order to transmute the fuel item to the next one. these states will be used 
by the block entities to determine if they should spread their EMC to the surrounding block entities or not. */

// This interface originally used SuperNumber, then switched to long, so forgive me if the arithmetic looks strange.
public interface ConsumerBlockEntity {
	long getEmc();
	void setEmc(long emc);
	long getOutputRate();
	default long getMaximumEmc() {
		return Long.MAX_VALUE;
	}
	default long getBonusEmc() {
		return 0;
	}
	boolean isConsuming();
	default void distributeEmc(List<BlockEntity> neighbors) {
		// stores the neighbors which will be given EMC (as a gift!)
		List<ConsumerBlockEntity> goodNeighbors = new ArrayList<ConsumerBlockEntity>(); 
		for (int i = 0; i < neighbors.size(); i++) {
			BlockEntity neighbor = neighbors.get(i);
			if (neighbor instanceof ConsumerBlockEntity consumerNeighbor
					&& consumerNeighbor.isConsuming()) {

				long max = consumerNeighbor.getMaximumEmc();
				if (max == 0 || consumerNeighbor.getEmc() < max)
					goodNeighbors.add(consumerNeighbor);
			}
		}
		if (goodNeighbors.size() > 0) {
			long emc = getEmc();
			long output = Long.min(getOutputRate(), emc);
			output /= goodNeighbors.size();
			for (ConsumerBlockEntity neighbor : goodNeighbors) {
				long neighborEmc = neighbor.getEmc();
				long neighborMaximumEmc = neighbor.getMaximumEmc();
				long neighborBonusEmc = neighbor.getBonusEmc();

				// If the neighbor is going to go over its maximum EMC,
				// only give it the exact amount it needs.
				if (neighborEmc >= neighborMaximumEmc - output - neighborBonusEmc) {
					long difference = neighborMaximumEmc;
					difference -= neighborEmc;
					neighborEmc = neighborMaximumEmc;
					emc -= difference;
				}
				else {
					neighborEmc += output + neighborBonusEmc;
					emc -= output;
				}

				neighbor.setEmc(neighborEmc);
			}
			setEmc(emc);
		}
	}
	default void serverSync(BlockPos pos, long emc, List<ServerPlayerEntity> list) {
		if (list.size() == 0)
			return;
		Iterator<ServerPlayerEntity> iterator = list.iterator();
		while (iterator.hasNext()) {
			ServerPlayerEntity player = iterator.next();
			if (player.currentScreenHandler instanceof LeveledScreenHandler screenHandler
					&& pos.equals(screenHandler.getPos())) 
				ModServerToClientPackets.UPDATE_CONSUMER_BLOCK.send(player, pos, emc);
			else
				iterator.remove();
		}
	}

	void update(long emc);
}