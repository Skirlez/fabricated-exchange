package com.skirlez.fabricatedexchange.util;

import java.math.BigInteger;

public class PlayerState {
    public BigInteger emc = BigInteger.ZERO;   
    private ServerState serverState;

    public PlayerState(ServerState serverState) {
        this.serverState = serverState;
    }
    public void markDirty() {
        this.serverState.markDirty();
    }
}
