package com.skirlez.fabricatedexchange.util;

import java.util.HashSet;

public class PlayerState {
    public SuperNumber emc = SuperNumber.Zero();   
    public HashSet<String> knowledge = new HashSet<String>();
    private ServerState serverState;
    public PlayerState(ServerState serverState) {
        this.serverState = serverState;
    }
    public void markDirty() {
        this.serverState.markDirty();
    }
}
