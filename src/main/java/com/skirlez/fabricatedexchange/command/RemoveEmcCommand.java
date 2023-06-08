package com.skirlez.fabricatedexchange.command;

import java.util.Map;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.skirlez.fabricatedexchange.emc.EmcData;
import com.skirlez.fabricatedexchange.util.ModConfig;
import com.skirlez.fabricatedexchange.util.SuperNumber;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

public class RemoveEmcCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(((CommandManager.literal("removeemc").requires(source -> source.hasPermissionLevel(3)))
            .then(CommandManager.literal("seed")
            .executes(context -> run(context, true))))
            .then(CommandManager.literal("custom")
            .executes(context -> run(context, false))));
    }

    private static int run(CommandContext<ServerCommandSource> context, boolean seed) {
        PlayerEntity p = context.getSource().getPlayer();

        ItemStack stack = p.getStackInHand(Hand.MAIN_HAND);
        if (stack.isEmpty()) {
            stack = p.getStackInHand(Hand.OFF_HAND);
            if (stack.isEmpty()) {
                context.getSource().sendFeedback(Text.translatable("commands.fabricated-exchange.no_item"), false);
                return 0;
            }
        }
        
        Item item = stack.getItem();
        String id = Registries.ITEM.getId(item).toString();
        if (seed) {
            Map<String, SuperNumber> map = ModConfig.SEED_EMC_MAP_FILE.getValue();
            if (!map.containsKey(id)) {
                context.getSource().sendFeedback(Text.translatable("commands.fabricated-exchange.removeemc.seed_confused")
                    .append(" ").append(Text.translatable("commands.fabricated-exchange.zero_notice")), false);
                return 0;
            }
            map.remove(id);
            ModConfig.SEED_EMC_MAP_FILE.save();
            context.getSource().sendFeedback(Text.translatable("commands.fabricated-exchange.removeemc.seed_success")
                .append(" ").append(Text.translatable("commands.fabricated-exchange.reload_notice")), false);
        }
        else {
            Map<String, SuperNumber> map = ModConfig.CUSTOM_EMC_MAP_FILE.getValue();
            if (!map.containsKey(id)) {
                context.getSource().sendFeedback(Text.translatable("commands.fabricated-exchange.removeemc.custom_confused")
                    .append(" ").append(Text.translatable("commands.fabricated-exchange.zero_notice")), false);
                return 0;
            }
            map.remove(id);
            ModConfig.CUSTOM_EMC_MAP_FILE.save();
            EmcData.syncMap(context.getSource().getPlayer());
            context.getSource().sendFeedback(Text.translatable("commands.fabricated-exchange.removeemc.custom_success")
                .append(" ").append(Text.translatable("commands.fabricated-exchange.reload_notice")), false);
        }

        return 1;
    }
}
