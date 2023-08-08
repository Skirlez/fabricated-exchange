package com.skirlez.fabricatedexchange.item.tools;

import java.util.List;

import com.skirlez.fabricatedexchange.item.ChargeableItem;
import com.skirlez.fabricatedexchange.item.ExtraFunctionItem;
import com.skirlez.fabricatedexchange.sound.ModSounds;
import com.skirlez.fabricatedexchange.util.GeneralUtil;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
public class DarkMatterSword extends SwordItem implements ChargeableItem, ExtraFunctionItem {
    public DarkMatterSword(ToolMaterial material, int attackDamage, float attackSpeed, Settings settings) {
        super(material, attackDamage, attackSpeed, settings);
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
        return 2;
    }

    protected boolean entityCondition(Entity entity) {
        // Entities don't have tags for these things. why
        return (entity instanceof Monster) || (entity instanceof HostileEntity) || (entity instanceof PlayerEntity);
    }

    @Override
    public void doExtraFunction(ItemStack stack, ServerPlayerEntity player) {
        if (player.getAttackCooldownProgress(0.0f) >= 1.0f) {
            player.resetLastAttackedTicks();
            player.swingHand(Hand.MAIN_HAND);

            List<Entity> entities = player.getWorld()
                .getOtherEntities(player, GeneralUtil.boxAroundPos(player.getPos(), ChargeableItem.getCharge(stack) + 1), DarkMatterSword.this::entityCondition);

            for (int i = 0; i < entities.size(); i++) {
                Entity entity = entities.get(i);
                entity.damage(player.getDamageSources().playerAttack(player), getAttackDamage());
            }
            player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.ITEM_CHARGE, 
                SoundCategory.PLAYERS, 1, 1.0f);
        }
    }

    @Override 
    public void doExtraFunctionClient(ItemStack stack, ClientPlayerEntity player) {
        if (player.getAttackCooldownProgress(0.0f) >= 1.0f) {
            player.resetLastAttackedTicks();
            player.swingHand(Hand.MAIN_HAND, false);
        }
    }

}
