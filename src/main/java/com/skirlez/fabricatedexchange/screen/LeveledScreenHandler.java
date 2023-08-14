package com.skirlez.fabricatedexchange.screen;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.math.BlockPos;


// All screen handlers that have levels inherit from this class.
public abstract class LeveledScreenHandler extends ScreenHandler {
    protected PacketByteBuf buf;
    protected BlockPos pos;
    protected int level;
    protected LeveledScreenHandler(ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    public int getLevel() {
        return level;
    }

    public BlockPos getPos() {
        return pos;
    }
    
    // intended to be called by the screen instance of the subclasses
    @Nullable
    public PacketByteBuf getAndConsumeCreationBuffer() {
        if (buf == null)
            return null;
        PacketByteBuf copy = buf;
        buf = null;
        return copy;
    }
}