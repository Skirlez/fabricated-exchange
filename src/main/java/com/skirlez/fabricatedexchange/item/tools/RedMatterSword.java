package com.skirlez.fabricatedexchange.item.tools;

import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.item.ChargeableItem;
import com.skirlez.fabricatedexchange.item.ExtraFunctionItem;
import com.skirlez.fabricatedexchange.item.ItemWithModes;
import com.skirlez.fabricatedexchange.sound.ModSounds;
import com.skirlez.fabricatedexchange.util.GeneralUtil;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.List;

public class RedMatterSword extends SwordItem implements ChargeableItem, ExtraFunctionItem, ItemWithModes {
    public RedMatterSword(ToolMaterial material, int attackDamage, float attackSpeed, Settings settings) {
        super(material, attackDamage, attackSpeed, settings);
    }

    @Override
    public int getModeAmount() {
        return 2;
    }

    @Override
    public boolean modeSwitchCondition(ItemStack stack) {
        return ChargeableItem.getCharge(stack) != 0;
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

    protected boolean entityCondition(Entity entity, ItemStack stack) {
        // Get the current mode of the sword
        int mode = ItemWithModes.getMode(stack);
        if (mode == 1) {
            return (entity instanceof AnimalEntity);
        } else { // Default mode or any other mode targets the original entities
            return (entity instanceof Monster) || (entity instanceof HostileEntity) || (entity instanceof PlayerEntity);
        }
    }

    @Override
    public void doExtraFunction(ItemStack stack, ServerPlayerEntity player) {
        if (player.getAttackCooldownProgress(0.0f) >= 1.0f) {
            player.resetLastAttackedTicks();
            player.swingHand(Hand.MAIN_HAND);

            List<Entity> entities = player.getWorld()
                    .getOtherEntities(player, GeneralUtil.boxAroundPos(player.getPos(), ChargeableItem.getCharge(stack) * 3),
                            entity -> this.entityCondition(entity, stack));

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

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        int charge = ChargeableItem.getCharge(stack);
        if (charge == 0) {
            tooltip.add(Text.translatable("item.fabricated-exchange.mode_switch")
                    .append(" ")
                    .append(Text.translatable("item.fabricated-exchange.red_matter_pickaxe.uncharged")
                            .setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY))));
        }
        else
            ItemWithModes.addModeToTooltip(stack, tooltip);
    }
}
