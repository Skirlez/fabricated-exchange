package com.skirlez.fabricatedexchange.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.skirlez.fabricatedexchange.item.NbtItem;

public class PlayerState {
    public SuperNumber emc = SuperNumber.Zero();   
    public HashSet<String> knowledge = new HashSet<String>();
    public List<NbtItem> specialKnowledge = new ArrayList<NbtItem>();
    private ServerState serverState;
    public PlayerState(ServerState serverState) {
        this.serverState = serverState;
    }
    public void markDirty() {
        this.serverState.markDirty();
    }
}
