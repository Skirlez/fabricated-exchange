package com.skirlez.fabricatedexchange.item.rings;

import com.skirlez.fabricatedexchange.emc.EmcData;
import com.skirlez.fabricatedexchange.entities.FrozenThrownEntity;
import com.skirlez.fabricatedexchange.item.EmcStoringItem;
import com.skirlez.fabricatedexchange.item.rings.base.ShooterRing;
import com.skirlez.fabricatedexchange.util.GeneralUtil;
import com.skirlez.fabricatedexchange.util.SuperNumber;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import java.util.List;

public class IgnitionRing extends ShooterRing {
	public IgnitionRing(Settings settings) {
		super(settings);
	}

	@Override
	protected void applyPlayerEffects(PlayerEntity player) {
		player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 3, 0, true, false, false));
		if (player.isOnFire()) {
			player.extinguish();
		}
	}

	@Override
	protected boolean consumeEmcAndFireProjectile(ItemStack stack, PlayerEntity player, Vec3d direction, World world) {
		if (!EmcStoringItem.takeStoredEmcOrConsume(getProjectileCost(), stack, player.getInventory()))
			return false;

		SmallFireballEntity fireball = new SmallFireballEntity(world, player, direction.x, direction.y, direction.z);
		fireball.setPosition(player.getX(), player.getEyeY(), player.getZ());
		fireball.setVelocity(direction.x, direction.y, direction.z, 2.0f, 0f);
		world.spawnEntity(fireball);
		return true;
	}

	@Override
	protected void playShootSound(PlayerEntity player, World world) {
		Random random = world.getRandom();
		world.playSound(null, player.getBlockPos(), SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS,
			1.0f, (random.nextFloat() - random.nextFloat()) * 0.2f + 1.0f);
	}


	protected SuperNumber getProjectileCost() {
		return EmcData.getItemEmc(Items.FIRE_CHARGE);
	}
}
