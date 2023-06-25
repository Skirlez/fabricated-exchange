package com.skirlez.fabricatedexchange.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.util.ModConfig;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class ReloadEmcCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((CommandManager.literal("reloademc").requires(source -> source.hasPermissionLevel(3)))
            .executes(context -> run(context)));
    }

    private static int run(CommandContext<ServerCommandSource> context) {
        long startTime = System.nanoTime();
        MinecraftServer server = context.getSource().getServer();
        ModConfig.CUSTOM_EMC_MAP_FILE.fetch();
        ModConfig.SEED_EMC_MAP_FILE.fetch();
        ModConfig.BLACKLISTED_MAPPER_RECIPES_FILE.fetch();
        String log = FabricatedExchange.reloadEmcMap(server);
        FabricatedExchange.syncMaps(server);

        String add = (log.isEmpty()) ? "\nNo errors or warnings." : "\n" + log;
        context.getSource().sendFeedback(Text.translatable("commands.fabricated-exchange.reloademc.success", String.valueOf((System.nanoTime() - startTime) / 1000000)).append(add), false);
        return 1;
    }
}
