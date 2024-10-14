package com.skirlez.fabricatedexchange.item.tools;

import com.skirlez.fabricatedexchange.item.EmcStoringItem;
import com.skirlez.fabricatedexchange.item.ExtraFunctionItem;
import com.skirlez.fabricatedexchange.mixin.ItemAccessor;
import com.skirlez.fabricatedexchange.screen.MercurialEyeScreenHandler;
import com.skirlez.fabricatedexchange.util.GeneralUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.WeakHashMap;

public class MercurialEye extends Item implements EmcStoringItem, ExtraFunctionItem {
	private static final WeakHashMap<PlayerEntity, BlockPos[]> state = new WeakHashMap<PlayerEntity, BlockPos[]>();

	public MercurialEye(Settings settings) {
		super(settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		BlockHitResult result = ItemAccessor.invokeRaycast(world, player, RaycastContext.FluidHandling.NONE);
		BlockPos pos;
		String response;
		TypedActionResult<ItemStack> actionResult;
		if (result.getType() == HitResult.Type.BLOCK) {
			pos = result.getBlockPos();
			response = "item.fabricated-exchange.mercurial_eye.corner_set";
			actionResult = TypedActionResult.success(player.getStackInHand(hand));
		}
		else {
			pos = player.getBlockPos();
			response = "item.fabricated-exchange.mercurial_eye.corner_set_player";
			actionResult = TypedActionResult.pass(player.getStackInHand(hand));
		}

		BlockPos[] positions = state.computeIfAbsent(player, p -> new BlockPos[2]);
		int index = (player.isSneaking()) ? 1 : 0;
		positions[index] = pos;
		if (world.isClient())
			GeneralUtil.showOverlayMessage(Text.translatable(response, index + 1, pos.getX(), pos.getY(), pos.getZ()));
		return actionResult;
	}

	public static BlockPos[] getPlayerBlocks(PlayerEntity player) {
		if (!state.containsKey(player))
			return new BlockPos[0];
		BlockPos[] positions = state.get(player);
		if (positions[0] == null && positions[1] == null)
			return new BlockPos[0];

		if (positions[0] != null && positions[1] == null)
			return new BlockPos[] {positions[0]};
		if (positions[0] == null && positions[1] != null)
			return new BlockPos[] {positions[1]};
		return positions;
	}


	private static NamedScreenHandlerFactory factory = new NamedScreenHandlerFactory() {
		@Override
		public Text getDisplayName() {
			return Text.empty();
		}

		public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
			return new MercurialEyeScreenHandler(syncId, playerInventory);
		}
	};

	@Override
	public void doExtraFunction(World world, PlayerEntity player, ItemStack stack) {
		player.openHandledScreen(factory);
	}
}
