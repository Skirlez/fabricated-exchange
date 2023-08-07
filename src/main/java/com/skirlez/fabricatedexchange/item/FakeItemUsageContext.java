package com.skirlez.fabricatedexchange.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;


// This class allows you to create "fake" usage contexts, manually setting the position of the block and its side without a raycast
public class FakeItemUsageContext extends ItemUsageContext {

    private final BlockPos pos;
    private final Direction side;
    public FakeItemUsageContext(PlayerEntity player, Hand hand, BlockPos pos, Direction side) {
        super(player, hand, null);
        this.pos = pos;
        this.side = side;
    }

    public BlockPos getBlockPos() {
        return pos;
    }

    public Direction getSide() {
        return side;
    }

    public Vec3d getHitPos() {
        return pos.toCenterPos();
    }

    public boolean hitsInsideBlock() {
        return true;
    }

}
