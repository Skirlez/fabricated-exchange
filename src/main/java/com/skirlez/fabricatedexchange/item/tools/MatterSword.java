package com.skirlez.fabricatedexchange.item.tools;

import com.skirlez.fabricatedexchange.item.*;
import com.skirlez.fabricatedexchange.sound.ModSounds;
import com.skirlez.fabricatedexchange.util.GeneralUtil;
import com.skirlez.fabricatedexchange.util.SuperNumber;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class MatterSword extends SwordItem implements ChargeableItem, ExtraFunctionItem, ItemWithModes,
		EmcStoringItem {
	
	
	private final int maxCharge;
	private final ParticleEffect particle;
	public MatterSword(ToolMaterial material, int attackDamage, float attackSpeed, Settings settings) {
		super(material, attackDamage, attackSpeed, settings);
		
		if (material == ModToolMaterials.DARK_MATTER_MATERIAL) {
			maxCharge = 2;
			particle = ParticleTypes.SMOKE;
		}
		else {
			maxCharge = 3;
			particle = DustParticleEffect.DEFAULT;
		}
	}
	
	private double getRadius(ItemStack stack) {
		return ChargeableItem.getCharge(stack) * 2 + 2;
	}

	@Override
	public int getModeAmount() {
		return 2;
	}

	@Override
	public boolean modeSwitchCondition(ItemStack stack) {
		return (getMaterial() == ModToolMaterials.RED_MATTER_MATERIAL);
	}
	
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
	public int getMaxCharge() {
		return maxCharge;
	}
	
	@Override
	public void onCharge(ItemStack stack, int charge, PlayerEntity player) {
		World world = player.getWorld();
		if (!world.isClient())
			return;
		Vec3d pos = player.getPos();
		int step = 3;
		double radius = getRadius(stack);
		for (int i = 0; i < 360; i += step) {
			double sin = Math.sin(Math.toRadians(i));
			double cos = Math.cos(Math.toRadians(i));
			world.addParticle(particle, pos.x + sin * radius, pos.y + 0.5d, pos.z + cos * radius, 0d, 0d, 0d);
			//world.addParticle(DustParticleEffect.DEFAULT, pos.x + sin * radius, pos.y + cos * radius, pos.z, 0d, 0d, 0d);
		}
	}

	protected boolean entityCondition(Entity entity, ItemStack stack) {
		// Get the current mode of the sword
		int mode = ItemWithModes.getMode(stack);
		if (mode == 1) {
			return (entity instanceof AnimalEntity);
		} else { // Default mode or any other mode targets the original entities
			return (entity instanceof Monster) || (entity instanceof HostileEntity) || (entity instanceof PlayerEntity);
		}
	}

	
	private static final SuperNumber BASE_AMOUNT = new SuperNumber(64);
	@Override
	public void doExtraFunction(World world, PlayerEntity player, ItemStack stack) {
		if (player.getAttackCooldownProgress(0.0f) < 1.0f)
			return;
		SuperNumber amount = new SuperNumber(BASE_AMOUNT);
		amount.multiply(ChargeableItem.getCharge(stack) + 1);
		if (!EmcStoringItem.takeStoredEmcOrConsume(amount, stack, player.getInventory())) {
			if (world.isClient())
				EmcStoringItem.showNoEmcMessage();
			return;
		}
		player.resetLastAttackedTicks();
		player.swingHand(Hand.MAIN_HAND);

		double radius = getRadius(stack);
		List<Entity> entities = player.getWorld()
				.getOtherEntities(player, GeneralUtil.boxAroundPos(player.getPos(), radius),
						entity -> this.entityCondition(entity, stack));

		for (int i = 0; i < entities.size(); i++) {
			Entity entity = entities.get(i);
			if (entity.distanceTo(player) <= radius)
				entity.damage(player.getDamageSources().playerAttack(player), getAttackDamage());
		}
		
		player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.ITEM_CHARGE, 
			SoundCategory.PLAYERS, 1, 1.0f);
		
	}


	@Override
	public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
		super.appendTooltip(stack, world, tooltip, context);
		ToolMaterial material = getMaterial();
		if (material == ModToolMaterials.RED_MATTER_MATERIAL)
			ItemWithModes.addModeToTooltip(stack, tooltip);
		 
	}
}
