package com.skirlez.fabricatedexchange.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.skirlez.fabricatedexchange.emc.EmcData;
import com.skirlez.fabricatedexchange.util.SuperNumber;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

public class SetEmcCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(((CommandManager.literal("setemc").requires(source -> source.hasPermissionLevel(3)))
            .then(CommandManager.literal("seed")
            .then(CommandManager.argument("number", StringArgumentType.string())
            .executes(context -> run(context, true)))))
            .then(CommandManager.literal("custom")
            .then(CommandManager.argument("number", StringArgumentType.string())
            .executes(context -> run(context, false)))));
    }

    private static int run(CommandContext<ServerCommandSource> context, boolean seed) {
        PlayerEntity p = context.getSource().getPlayer();
        String str = context.getArgument("number", String.class);

        if (!str.matches("[\\d/]+")) {
            context.getSource().sendFeedback(MutableText.of(Text.of(str).getContent()).append(Text.translatable("commands.fabricated-exchange.setemc.not_valid_number")), false);
            return 0;
        }
        SuperNumber num = new SuperNumber(str);
        ItemStack stack = p.getStackInHand(Hand.MAIN_HAND);
        if (stack.isEmpty()) {
            stack = p.getStackInHand(Hand.OFF_HAND);
            if (stack.isEmpty()) {
                context.getSource().sendFeedback(Text.translatable("commands.fabricated-exchange.no_item"), false);
                return 0;
            }
        }
        Item item = stack.getItem();
        EmcData.setItemEmc(item, num, seed);
        if (seed) 
            context.getSource().sendFeedback(Text.translatable("commands.fabricated-exchange.setemc.seed_success")
            .append(" ").append(Text.translatable("commands.fabricated-exchange.reload_notice")), false);
        else {
            EmcData.syncMap(context.getSource().getPlayer());
            context.getSource().sendFeedback(Text.translatable("commands.fabricated-exchange.setemc.custom_success"), false);
        }

        return 1;
    }
}
