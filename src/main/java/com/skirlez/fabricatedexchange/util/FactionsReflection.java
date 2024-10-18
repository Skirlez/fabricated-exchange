package com.skirlez.fabricatedexchange.util;

import com.skirlez.fabricatedexchange.FabricatedExchange;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.lang.reflect.Method;

// Reflection to check if https://github.com/ickerio/factions allows placing blocks in a place.
// There doesn't seem to be any API for this...
public class FactionsReflection {
	private static boolean reflectionSuccessful;

	private static Method checkPermissionsMethod;
	private static Object breakBlocksPermission;

	static {
		try {
			Class<?> interactionManagerClass = Class.forName("io.icker.factions.core.InteractionManager");
			Class<?> permissionsClass = Class.forName("io.icker.factions.api.persistents.Relationship.Permissions");
			breakBlocksPermission = permissionsClass.getDeclaredField("BREAK_BLOCKS").get(null);
			checkPermissionsMethod = interactionManagerClass.getDeclaredMethod("checkPermissions",
				PlayerEntity.class, BlockPos.class, World.class, permissionsClass);
			FabricatedExchange.LOGGER.info("Factions mod detected! Fabricated Exchange will ask it nicely before its items place blocks.");
			reflectionSuccessful = true;
		}
		catch (Exception ignored) {
			reflectionSuccessful = false;
		}
	}

	public static boolean canPlayerPlace(PlayerEntity player, BlockPos pos) {
		if (!reflectionSuccessful)
			return true;

		try {
			ActionResult result = (ActionResult) checkPermissionsMethod.invoke(null, player, pos, player.getWorld(), breakBlocksPermission);
			return (result != ActionResult.FAIL);
		}
		catch (Exception ignored) {
			return true;
		}
	}


}
