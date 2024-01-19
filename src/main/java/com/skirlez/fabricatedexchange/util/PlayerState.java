package com.skirlez.fabricatedexchange.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.skirlez.fabricatedexchange.item.NbtItem;
import net.minecraft.item.Item;

public class PlayerState {
	public SuperNumber emc = SuperNumber.Zero();   
	public Set<Item> knowledge = Collections.synchronizedSet(new HashSet<Item>());
	public List<NbtItem> specialKnowledge = Collections.synchronizedList(new ArrayList<NbtItem>());
	private ServerState serverState;
	public PlayerState(ServerState serverState) {
		this.serverState = serverState;
	}
	public void markDirty() {
		this.serverState.markDirty();
	}
}
