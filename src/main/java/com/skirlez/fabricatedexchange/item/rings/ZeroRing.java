package com.skirlez.fabricatedexchange.item.rings;

import com.skirlez.fabricatedexchange.emc.EmcData;
import com.skirlez.fabricatedexchange.entities.FrozenThrownEntity;
import com.skirlez.fabricatedexchange.item.EmcStoringItem;
import com.skirlez.fabricatedexchange.item.rings.base.ShooterRing;
import com.skirlez.fabricatedexchange.util.GeneralUtil;
import com.skirlez.fabricatedexchange.util.SuperNumber;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public class ZeroRing extends ShooterRing {
	
	public ZeroRing(Settings settings) {
		super(settings);
	}

	@Override
	protected boolean consumeEmcAndFireProjectile(ItemStack stack, PlayerEntity player, Vec3d direction, World world) {
		if (!EmcStoringItem.takeStoredEmcOrConsume(getProjectileCost(), stack, player.getInventory()))
			return false;
		FrozenThrownEntity frozenProjectile = new FrozenThrownEntity(world, player);
		frozenProjectile.setPosition(player.getX(), player.getEyeY(), player.getZ());
		frozenProjectile.setVelocity(direction.x, direction.y, direction.z, 3.0f, 0f);
		world.spawnEntity(frozenProjectile);
		return true;
	}

	@Override
	protected void playShootSound(PlayerEntity player, World world) {

	}


	private static SuperNumber FAILSAFE_COST = new SuperNumber(16);

	protected SuperNumber getProjectileCost() {
		SuperNumber powderSnowBucketEmc = EmcData.getItemEmc(Items.POWDER_SNOW_BUCKET);
		SuperNumber bucketEmc = EmcData.getItemEmc(Items.BUCKET);
		if (powderSnowBucketEmc.equalsZero() || bucketEmc.equalsZero())
			return FAILSAFE_COST;
		powderSnowBucketEmc.subtract(bucketEmc);
		return bucketEmc;
	}



}
