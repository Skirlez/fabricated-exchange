package com.skirlez.fabricatedexchange.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.mixin.ItemAccessor;
import com.skirlez.fabricatedexchange.screen.BlocklessCraftingScreenHandler;
import com.skirlez.fabricatedexchange.sound.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PhilosophersStone extends Item implements ChargeableItem, ExtraFunctionItem, OutliningItem {
    public PhilosophersStone(Settings settings) {
        super(settings);
        ItemAccessor self = (ItemAccessor) this;
        self.setRecipeRemainder(this);
    }
    private Random r = new Random();

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        return ChargeableItem.COLOR;
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        return ChargeableItem.getItemBarStep(stack, getMaxCharge());
    }


    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        BlockPos pos = context.getBlockPos();
        World world = context.getWorld();
        Block block = world.getBlockState(pos).getBlock();
        boolean valid = FabricatedExchange.blockTransmutationMap.containsKey(block);
        if (valid) {
            if (world.isClient()) {
                context.getPlayer().playSound(ModSounds.PS_USE, 1F, 1F);
                for (int i = 0; i < 3; i++) {
                    world.addParticle(ParticleTypes.LARGE_SMOKE, 
                        (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, 
                        (double)pos.getZ() + 0.5, r.nextDouble(0.2) - 0.1, 0.06, r.nextDouble(0.2) - 0.1);
                }
            }
            else {
                ItemStack stack = context.getStack();
                int charge = ChargeableItem.getCharge(stack);
                if (charge == 0) 
                    world.setBlockState(pos, FabricatedExchange.blockTransmutationMap.get(block).getDefaultState());
                else {
                    List<BlockPos> positions = getPositionsToOutline(context.getPlayer(), stack, pos);
                    for (BlockPos newPos : positions)
                        world.setBlockState(newPos, FabricatedExchange.blockTransmutationMap.get(block).getDefaultState());
                }
            }
        }
        return ActionResult.success(valid);
    }



    private final Text TITLE = Text.translatable("container.crafting");

    @Override
    public void doExtraFunction(ItemStack stack, ServerPlayerEntity player) {
        player.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, inventory, player2) 
            -> new BlocklessCraftingScreenHandler(syncId, inventory), TITLE));
    }

    @Override
    public boolean outlineEntryCondition(BlockState state) {
        return FabricatedExchange.blockTransmutationMap.containsKey(state.getBlock());
    }

    @Override
    public List<BlockPos> getPositionsToOutline(PlayerEntity player, ItemStack stack, BlockPos center) {
        List<BlockPos> list = new ArrayList<BlockPos>();
        World world = player.getWorld();

        Block block = world.getBlockState(center).getBlock();
        int size = ChargeableItem.getCharge(stack);
        center = center.add(-size, -size, -size);
        int len = size * 2 + 1;
        for (int x = 0; x < len; x++) {
            for (int y = 0; y < len; y++) {
                for (int z = 0; z < len; z++) {
                    BlockPos newPos = center.add(x, y, z);
                    if ((world.getBlockState(newPos).getBlock().equals(block)))
                        list.add(newPos);
                }
            }
        }
        return list;
    }
}

