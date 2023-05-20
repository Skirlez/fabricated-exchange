package com.skirlez.fabricatedexchange.util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class PlayerState {
    public BigInteger emc = BigInteger.ZERO;   
    public List<String> knowledge = new ArrayList<String>();
    private ServerState serverState;

    public PlayerState(ServerState serverState) {
        this.serverState = serverState;
    }
    public void markDirty() {
        this.serverState.markDirty();
    }
}
