package com.skirlez.fabricatedexchange.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.skirlez.fabricatedexchange.util.config.ModConfig;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class ClientCommand {
        
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        LiteralCommandNode<FabricClientCommandSource> mainNode = ClientCommandManager
        .literal("feclient") 
        .build();
        
        LiteralCommandNode<FabricClientCommandSource> reloadConfigNode = ClientCommandManager
        .literal("reload") 
        .executes(context -> reloadConfig(context))
        .build();

        dispatcher.getRoot().addChild(mainNode);

        mainNode.addChild(reloadConfigNode);

    }

    private static int reloadConfig(CommandContext<FabricClientCommandSource> context) {
        ModConfig.fetchAll();
        context.getSource().sendFeedback(Text.literal("Data files reloaded!"));
        return 1;
    }



}
