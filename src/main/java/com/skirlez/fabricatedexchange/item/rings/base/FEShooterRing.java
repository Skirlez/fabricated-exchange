package com.skirlez.fabricatedexchange.item.rings.base;

import com.skirlez.fabricatedexchange.item.EmcStoringItem;
import com.skirlez.fabricatedexchange.item.ExtraFunctionItem;
import com.skirlez.fabricatedexchange.item.ItemWithModes;
import com.skirlez.fabricatedexchange.mixin.ItemAccessor;
import com.skirlez.fabricatedexchange.util.SuperNumber;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.Random;

public abstract class FEShooterRing extends Item implements ExtraFunctionItem, ItemWithModes {

    private static final SuperNumber DESIRED_AMOUNT = new SuperNumber(98);
    private final Random random = new Random();

    public boolean autoshoot = false;

    public FEShooterRing(Settings settings) {
        super(settings);
        ItemAccessor self = (ItemAccessor) this;
        self.setRecipeRemainder(this);
    }

    @Override
    public void doExtraFunction(ItemStack stack, ServerPlayerEntity player) {
        autoshoot = !autoshoot;
    }

    @Override
    public int getModeAmount() {
        return 4;
    }

    @Override
    public boolean modeSwitchCondition(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getDefaultStack() {
        ItemStack stack = new ItemStack(this);
        stack.getOrCreateNbt().putString(EmcStoringItem.EMC_NBT_KEY, "0");
        return stack;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        fireLogic(world, user, stack);
        return TypedActionResult.success(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity pEntity, int slot, boolean selected) {
        SuperNumber storedEmc = EmcStoringItem.getStoredEmc(stack);
        SuperNumber projectileEMC = getProjectileEMC();

        if (pEntity instanceof PlayerEntity player) {
            if (storedEmc.toDouble() < (projectileEMC.toDouble() * 7)) {
                storedEmc = EmcStoringItem.tryConsumeEmc(DESIRED_AMOUNT, stack, player.getInventory());

                if (storedEmc.toDouble() < projectileEMC.toDouble())
                    return;
            }
            if (!storedEmc.isPositive())
                storedEmc = EmcStoringItem.tryConsumeEmc(DESIRED_AMOUNT, stack, player.getInventory());
            EmcStoringItem.setStoredEmc(stack, storedEmc);

            applyPlayerEffects(player);
        }

        if (autoshoot) {
            stack.getOrCreateNbt().putInt("CustomModelData", 1);
            fireLogic(world, (PlayerEntity) pEntity, stack);
        } else {
            stack.getOrCreateNbt().putInt("CustomModelData", 0);
        }
    }

    protected abstract SuperNumber getProjectileEMC();

    protected abstract void fireSingleProjectile(World world, PlayerEntity user, float speed, float divergence);

    protected abstract boolean fireHomingProjectile(World world, PlayerEntity user, float speed, float divergence);

    protected abstract void fireChaosProjectile(World world, PlayerEntity user, float speed, float divergence);

    protected void applyPlayerEffects(PlayerEntity player) {
        // Override to add specific player effects in subclasses
    }

    protected boolean isVisible(PlayerEntity user, LivingEntity target, World world) {
        Vec3d userEyes = user.getCameraPosVec(1.0F);
        Vec3d targetEyes = target.getPos().add(0, target.getEyeHeight(target.getPose()), 0);
        RaycastContext context = new RaycastContext(userEyes, targetEyes, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, user);
        BlockHitResult result = world.raycast(context);
        return result.getType() == HitResult.Type.MISS || result.getPos().squaredDistanceTo(targetEyes) < 0.5;
    }

    private void fireLogic(World world, PlayerEntity user, ItemStack stack) {
        SuperNumber storedEmc = EmcStoringItem.getStoredEmc(stack);
        int mode = ItemWithModes.getMode(stack);

        if (!world.isClient) {
            var projectileEMC = getProjectileEMC();
            if (projectileEMC.equalsZero()) {
                projectileEMC = new SuperNumber(14);
            }

            switch (mode) {
                case 0: {
                    if (storedEmc.toDouble() >= projectileEMC.toDouble() && !user.getItemCooldownManager().isCoolingDown(this)) {
                        storedEmc.subtract(projectileEMC);
                        fireSingleProjectile(world, user, 5.0F, 0.0F);
                        user.getItemCooldownManager().set(this, 5);
                    }
                    break;
                }
                case 1: {
                    if (storedEmc.toDouble() >= (projectileEMC.toDouble() * 7) && !user.getItemCooldownManager().isCoolingDown(this)) {
                        for (int i = 0; i < 7; i++) {
                            storedEmc.subtract(projectileEMC);
                            fireSingleProjectile(world, user, 3.5F, 6.0F);
                        }
                        user.getItemCooldownManager().set(this, 10);
                    }
                    break;
                }
                case 2: {
                    if (storedEmc.toDouble() >= projectileEMC.toDouble() * 2) {
                        SuperNumber doubleProjectileEMC = projectileEMC;
                        doubleProjectileEMC.multiply(2);
                        if (fireHomingProjectile(world, user, 3.5f, 0.0f)) {
                            storedEmc.subtract(doubleProjectileEMC);
                        }
                    }
                    break;
                }
                case 3: {
                    if (storedEmc.toDouble() >= projectileEMC.toDouble() * 7) {
                        for (int i = 0; i < 7; i++) {
                            storedEmc.subtract(projectileEMC);
                            fireChaosProjectile(world, user, 5.0F, 1.0F);
                        }
                        user.getItemCooldownManager().set(this, 5);
                    }
                    break;
                }
            }
        }
        EmcStoringItem.setStoredEmc(stack, storedEmc);
    }
}
