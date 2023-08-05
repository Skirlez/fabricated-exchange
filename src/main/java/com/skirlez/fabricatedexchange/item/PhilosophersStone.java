package com.skirlez.fabricatedexchange.item;

import java.util.Random;
import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.mixin.ItemAccessor;
import com.skirlez.fabricatedexchange.screen.BlocklessCraftingScreenHandler;
import com.skirlez.fabricatedexchange.sound.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class PhilosophersStone extends Item implements ChargeableItem, ExtraFunctionItem {
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
        BlockPos blockPos = context.getBlockPos();
        World world = context.getWorld();
        Block block = world.getBlockState(blockPos).getBlock();
        boolean valid = FabricatedExchange.blockTransmutationMap.containsKey(block);
        if (valid) {
            if (world.isClient()) {
                context.getPlayer().playSound(ModSounds.PS_USE, 1F, 1F);
                for (int i = 0; i < 3; i++) {
                    world.addParticle(ParticleTypes.LARGE_SMOKE, 
                        (double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, 
                        (double)blockPos.getZ() + 0.5, r.nextDouble(0.2) - 0.1, 0.06, r.nextDouble(0.2) - 0.1);
                }
            }
            else
                switchBlock(world, blockPos, context.getStack(), context.getSide());
        }
        return ActionResult.success(valid);
    }

    private void switchBlock(World world, BlockPos pos, ItemStack stack, Direction d) {
        int charge = stack.getOrCreateNbt().getInt(CHARGE_KEY);
        Block block = world.getBlockState(pos).getBlock();
        if (charge == 0) {
            world.setBlockState(pos, FabricatedExchange.blockTransmutationMap.get(block).getDefaultState());
            return;
        }
        int xOff = -charge, yOff = -charge, zOff = -charge;
        switch (d) {
            case DOWN:
                yOff += charge;
                break;
            case UP:
                yOff -= charge;
                break;
            case NORTH:
                zOff += charge;
                break;
            case SOUTH:
                zOff -= charge;
                break;
            case WEST:
                xOff += charge;
                break;
            case EAST:
                xOff -= charge;
                break;
            default:
                FabricatedExchange.LOGGER.error("Unknown block side philospher's stone block replacement. Side: " + d.toString());
                break;
        };
        pos = pos.add(xOff, yOff, zOff);
        int len = charge * 2 + 1;
        for (int x = 0; x < len; x++) {
            for (int y = 0; y < len; y++) {
                for (int z = 0; z < len; z++) {
                    BlockPos newPos = pos.add(x, y, z);
                    Block newBlock = world.getBlockState(newPos).getBlock();
                    if (!newBlock.equals(block))
                        continue;
                    world.setBlockState(newPos, FabricatedExchange.blockTransmutationMap.get(block).getDefaultState());
                }
            }
        }
        
    }

    private final Text TITLE = Text.translatable("container.crafting");

    @Override
    public void doExtraFunction(ItemStack stack, ServerPlayerEntity player) {
        player.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, inventory, player2) 
            -> new BlocklessCraftingScreenHandler(syncId, inventory), TITLE));
    }

    


}

