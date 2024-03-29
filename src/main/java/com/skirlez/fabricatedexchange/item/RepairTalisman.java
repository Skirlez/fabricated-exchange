package com.skirlez.fabricatedexchange.item;

import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.block.AlchemicalChestBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class RepairTalisman extends Item {
    private int tick;
    public RepairTalisman(Settings settings) {
        super(settings);
        this.tick = 0;
    }

    // TODO: make only a single repair talisman in inventory effective
    // TODO: work in alchemical chests
    // TODO: work in alchemical bags (once implemented)
    // TODO: work in pedestals (once implemented)
    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);

        if (world.isClient) {
            return;
        }

        if (entity instanceof PlayerEntity player) {
            tick++;
            if (tick < 20) {
                return;
            }
            PlayerInventory inventory = player.getInventory();
            for (int i = 0; i < inventory.size(); i++) {
                ItemStack itemStack = inventory.getStack(i);
                ItemStack held_item = player.getMainHandStack();
                if (!itemStack.isDamaged()) {
                    continue;
                }
                if (itemStack.isItemEqual(held_item) && player.handSwinging) {
                    continue;
                }
                itemStack.setDamage(itemStack.getDamage() - 1);
            }
            tick = 0;
        }
    }
}
