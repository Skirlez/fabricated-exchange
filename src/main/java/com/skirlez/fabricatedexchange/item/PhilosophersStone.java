package com.skirlez.fabricatedexchange.item;

import com.skirlez.fabricatedexchange.BlockTransmutation;
import com.skirlez.fabricatedexchange.mixin.ItemAccessor;
import com.skirlez.fabricatedexchange.screen.BlocklessCraftingScreenHandler;
import com.skirlez.fabricatedexchange.sound.ModSounds;
import com.skirlez.fabricatedexchange.util.GeneralUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;


// The philosopher's stone implements nearly all the special item interfaces. A good example item, I'd say.
public class PhilosophersStone extends Item
		implements ChargeableItem, ExtraFunctionItem, OutliningItem, ItemWithModes {
	
	public PhilosophersStone(Settings settings) {
		super(settings);
		ItemAccessor self = (ItemAccessor) this;
		self.setRecipeRemainder(this);
	}
	private static Random r = new Random();

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
		boolean valid = BlockTransmutation.canTransmuteBlock(block);
		if (valid) {
			context.getPlayer().playSound(ModSounds.PS_USE, 1f, 1f);
			if (world.isClient()) {
				for (int i = 0; i < 3; i++) {
					world.addParticle(ParticleTypes.LARGE_SMOKE, 
						(double)pos.getX() + 0.5, (double)pos.getY() + 0.5, 
						(double)pos.getZ() + 0.5, r.nextDouble(0.2) - 0.1, 0.06, r.nextDouble(0.2) - 0.1);
				}
			}
			else {
				ItemStack stack = context.getStack();
				int charge = ChargeableItem.getCharge(stack);
				BlockState state = BlockTransmutation.getBlockToTransmute(block).get().getDefaultState();
				if (charge == 0) 
					world.setBlockState(pos, state);
				else {
					List<BlockPos> positions = getPositionsToOutline(context.getPlayer(), stack, pos);
					for (BlockPos newPos : positions)
						world.setBlockState(newPos, state);
				}
				
			}
		}
		return ActionResult.success(valid);
	}

	private final Text TITLE = Text.translatable("container.crafting");

	@Override
	public void doExtraFunction(World world, PlayerEntity player, ItemStack stack) {
		player.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, inventory, player2) 
			-> new BlocklessCraftingScreenHandler(syncId, inventory), TITLE));
	}

	@Override
	public boolean outlineEntryCondition(BlockState state) {
		return BlockTransmutation.canTransmuteBlock(state.getBlock());
	}

	@Override
	public List<BlockPos> getPositionsToOutline(PlayerEntity player, ItemStack stack, BlockPos center) {
		List<BlockPos> list = new ArrayList<BlockPos>();
		World world = player.getWorld();

		

		Block block = world.getBlockState(center).getBlock();
		int size = ChargeableItem.getCharge(stack); 
		int len = size * 2 + 1;
		Direction dir = ItemUtil.getHorizontalMineDirection(player, center).rotateYClockwise();

		int mode = ItemWithModes.getMode(stack);
		final BlockPos corner;
		corner = switch (mode) {
			case 0 -> center.add(-size, -size, -size);
			case 1 -> center.add(-size, 0, -size);
			case 2 -> center.offset(dir, -size).up(size);
			default -> center;
		};

		Consumer<int[]> cubeConsumer = new Consumer<int[]>() {
			public void accept(int[] t) {
				BlockPos newPos = corner.add(t[0], t[1], t[2]);
				if ((world.getBlockState(newPos).getBlock().equals(block)))
					list.add(newPos);
			}
		};
		Consumer<int[]> floorConsumer = new Consumer<int[]>() {
			public void accept(int[] t) {
				BlockPos newPos = corner.add(t[0], 0, t[1]);
				if ((world.getBlockState(newPos).getBlock().equals(block)))
					list.add(newPos);
			}
		};
		Consumer<int[]> wallConsumer = new Consumer<int[]>() {
			public void accept(int[] t) {
				BlockPos newPos = corner.offset(dir, t[0]).down(t[1]);
				if ((world.getBlockState(newPos).getBlock().equals(block)))
					list.add(newPos);
			}
		};


		switch (mode) {
			case 0 -> GeneralUtil.nestedLoop(3, len, cubeConsumer);
			case 1 -> GeneralUtil.nestedLoop(2, len, floorConsumer);
			case 2 -> GeneralUtil.nestedLoop(2, len, wallConsumer);
		}
		

		return list;
	}

	@Override
	public int getModeAmount() {
		return 3;
	}
	@Override
	public boolean modeSwitchCondition(ItemStack stack) {
		return ChargeableItem.getCharge(stack) != 0;
	}
	@Override
	public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
		super.appendTooltip(stack, world, tooltip, context);
		ItemUtil.addModeAndChargeToTooltip(stack, tooltip);
	}


}

