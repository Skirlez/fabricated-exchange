package com.skirlez.fabricatedexchange.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.skirlez.fabricatedexchange.emc.EmcData;
import com.skirlez.fabricatedexchange.util.ModConfig;
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
        dispatcher.register(CommandManager.literal("setcustomemc").requires(source -> source.hasPermissionLevel(3))
            .then(CommandManager.argument("number", StringArgumentType.word()).executes(SetEmcCommand::runCustom)));

        dispatcher.register(CommandManager.literal("setseedemc").requires(source -> source.hasPermissionLevel(3))
            .then(CommandManager.argument("number", StringArgumentType.word()).executes(SetEmcCommand::runSeed)));
    }

    private static int run(CommandContext<ServerCommandSource> context, boolean seed) {
        PlayerEntity p = context.getSource().getPlayer();
        String str = context.getArgument("number", String.class);

        if (!str.matches("[\\d/]+")) {
            context.getSource().sendFeedback(MutableText.of(Text.of(str).getContent()).append(Text.translatable("commands.fabricated-exchange.setemc.no_number")), false);
            return -1;
        }
        SuperNumber num = new SuperNumber(str);
        ItemStack stack = p.getStackInHand(Hand.MAIN_HAND);
        if (stack.isEmpty()) {
            stack = p.getStackInHand(Hand.OFF_HAND);
            if (stack.isEmpty()) {
                context.getSource().sendFeedback(Text.translatable("commands.fabricated-exchange.setemc.no_item"), false);
                return -1;
            }
        }
        Item item = stack.getItem();
        if (seed) {
            EmcData.setItemEmc(item, num, ModConfig.SEED_EMC_MAP_FILE, false);
            context.getSource().sendFeedback(Text.translatable("commands.fabricated-exchange.setemc.seed_success"), false);
        }
        else {
            EmcData.setItemEmc(item, num, ModConfig.CUSTOM_EMC_MAP_FILE, true);
            context.getSource().sendFeedback(Text.translatable("commands.fabricated-exchange.setemc.custom_success"), false);
        }

        return 1;
    }

    public static int runSeed(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return run(context, true);
    }

    public static int runCustom(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return run(context, false);
    }
}
