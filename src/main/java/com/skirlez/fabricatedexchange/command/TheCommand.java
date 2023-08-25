package com.skirlez.fabricatedexchange.command;

import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.emc.EmcData;
import com.skirlez.fabricatedexchange.util.DataFile;
import com.skirlez.fabricatedexchange.util.SuperNumber;
import com.skirlez.fabricatedexchange.util.config.ModConfig;

import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.registry.Registry;

public class TheCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> mainNode = CommandManager
        // consider "/fabex" if /fe is taken
        // if it's taken after this mod releases i'm standing my ground (:
        // (why do commands not have namespaces?)
        .literal("fe") 
        .build();

        LiteralCommandNode<ServerCommandSource> helpNode = CommandManager
        .literal("help")
        .executes(context -> help(context))
        .build();
        
        LiteralCommandNode<ServerCommandSource> setNode = CommandManager
        .literal("set")
        .requires(source -> source.hasPermissionLevel(2))
        .build();
        LiteralCommandNode<ServerCommandSource> setSeedNode = CommandManager
        .literal("seed")
        .then(CommandManager.argument("number", StringArgumentType.greedyString())
        .executes(context -> setEmc(context, true)))
        .build();
        LiteralCommandNode<ServerCommandSource> setCustomNode = CommandManager
        .literal("custom")
        .then(CommandManager.argument("number", StringArgumentType.greedyString())
        .executes(context -> setEmc(context, false)))
        .build();


        LiteralCommandNode<ServerCommandSource> removeNode = CommandManager
        .literal("remove")
        .requires(source -> source.hasPermissionLevel(2))
        .build();
        LiteralCommandNode<ServerCommandSource> removeSeedNode = CommandManager
        .literal("seed")
        .executes(context -> removeEmc(context, true))
        .build();
        LiteralCommandNode<ServerCommandSource> removeCustomNode = CommandManager
        .literal("custom")
        .executes(context -> removeEmc(context, false))
        .build();

        LiteralCommandNode<ServerCommandSource> recipeNode = CommandManager
        .literal("recipe")
        .requires(source -> source.hasPermissionLevel(2))
        .build();
        
        LiteralCommandNode<ServerCommandSource> banRecipeNode = CommandManager
        .literal("ban")
        .then(CommandManager.argument("recipe", IdentifierArgumentType.identifier()).suggests(SuggestionProviders.ALL_RECIPES)
        .executes(context -> banRecipe(context, IdentifierArgumentType.getRecipeArgument(context, "recipe"))))
        .build();

        LiteralCommandNode<ServerCommandSource> pardonRecipeNode = CommandManager
        .literal("pardon")
        .then(CommandManager.argument("recipe", IdentifierArgumentType.identifier()).suggests(SuggestionProviders.ALL_RECIPES)
        .executes(context -> pardonRecipe(context, IdentifierArgumentType.getRecipeArgument(context, "recipe"))))
        .build();
        
        LiteralCommandNode<ServerCommandSource> reloadNode = CommandManager
        .literal("reload")
        .requires(source -> source.hasPermissionLevel(2))
        .executes(context -> reload(context))
        .build();

        LiteralCommandNode<ServerCommandSource> resetNode = CommandManager
        .literal("reset")
        .requires(source -> source.hasPermissionLevel(2))
        .then(CommandManager.argument("datafile", StringArgumentType.word()).suggests(new SuggestionProvider<ServerCommandSource>() {
            @Override
            public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context,
                    SuggestionsBuilder builder) throws CommandSyntaxException {
                builder.suggest("config");
                builder.suggest("seed_emc_map");
                builder.suggest("custom_emc_map");
                return builder.buildFuture();
            }
            
        })
        .executes(context -> reset(context)))
        .build();

        dispatcher.getRoot().addChild(mainNode);

        mainNode.addChild(helpNode);

        mainNode.addChild(setNode);
        setNode.addChild(setSeedNode);
        setNode.addChild(setCustomNode);

        mainNode.addChild(removeNode);
        removeNode.addChild(removeSeedNode);
        removeNode.addChild(removeCustomNode);

        mainNode.addChild(recipeNode);
        recipeNode.addChild(banRecipeNode);
        recipeNode.addChild(pardonRecipeNode);

        mainNode.addChild(reloadNode);

        mainNode.addChild(resetNode);

    }






    private static int help(CommandContext<ServerCommandSource> context) {
        context.getSource().sendMessage(Text.translatable("commands.fabricated-exchange.help"));
        return 1;    
    }

    private static int setEmc(CommandContext<ServerCommandSource> context, boolean seed) {
        PlayerEntity p = context.getSource().getPlayer();
        String str = context.getArgument("number", String.class);

        if (!str.matches("[\\d/]+")) {
            context.getSource().sendMessage(MutableText.of(Text.of(str).getContent()).append(Text.translatable("commands.fabricated-exchange.setemc.not_valid_number")));
            return 0;
        }
        SuperNumber num = new SuperNumber(str);
        ItemStack stack = p.getStackInHand(Hand.MAIN_HAND);
        if (stack.isEmpty()) {
            stack = p.getStackInHand(Hand.OFF_HAND);
            if (stack.isEmpty()) {
                context.getSource().sendMessage(Text.translatable("commands.fabricated-exchange.no_item"));
                return 0;
            }
        }
        Item item = stack.getItem();
        EmcData.setItemEmc(item, num, seed);
        if (seed) 
            context.getSource().sendMessage(Text.translatable("commands.fabricated-exchange.setemc.seed_success")
            .append(" ").append(Text.translatable("commands.fabricated-exchange.reload_notice")));
        else {
            FabricatedExchange.syncMaps(context.getSource().getServer());
            context.getSource().sendMessage(Text.translatable("commands.fabricated-exchange.setemc.custom_success"));
        }

        return 1;
    }

    private static int removeEmc(CommandContext<ServerCommandSource> context, boolean seed) {
        PlayerEntity p = context.getSource().getPlayer();

        ItemStack stack = p.getStackInHand(Hand.MAIN_HAND);
        if (stack.isEmpty()) {
            stack = p.getStackInHand(Hand.OFF_HAND);
            if (stack.isEmpty()) {
                context.getSource().sendMessage(Text.translatable("commands.fabricated-exchange.no_item"));
                return 0;
            }
        }
        
        Item item = stack.getItem();
        String id = Registry.ITEM.getId(item).toString();
        if (seed) {
            Map<String, SuperNumber> map = ModConfig.SEED_EMC_MAP_FILE.getValue();
            if (!map.containsKey(id)) {
                context.getSource().sendMessage(Text.translatable("commands.fabricated-exchange.removeemc.seed_confused")
                    .append(" ").append(Text.translatable("commands.fabricated-exchange.zero_notice")));
                return 0;
            }
            map.remove(id);
            ModConfig.SEED_EMC_MAP_FILE.setValueAndSave(map);
            context.getSource().sendMessage(Text.translatable("commands.fabricated-exchange.removeemc.seed_success")
                .append(" ").append(Text.translatable("commands.fabricated-exchange.reload_notice")));
        }
        else {
            Map<String, SuperNumber> map = ModConfig.CUSTOM_EMC_MAP_FILE.getValue();
            if (!map.containsKey(id)) {
                context.getSource().sendMessage(Text.translatable("commands.fabricated-exchange.removeemc.custom_confused")
                    .append(" ").append(Text.translatable("commands.fabricated-exchange.zero_notice")));
                return 0;
            }
            map.remove(id);
            ModConfig.CUSTOM_EMC_MAP_FILE.setValueAndSave(map);
            EmcData.syncMap(context.getSource().getPlayer());
            context.getSource().sendMessage(Text.translatable("commands.fabricated-exchange.removeemc.custom_success")
                .append(" ").append(Text.translatable("commands.fabricated-exchange.reload_notice")));
        }

        return 1;
    }

    private static int banRecipe(CommandContext<ServerCommandSource> context, Recipe<?> recipe) {
        String type = recipe.getType().toString();
        if (!isRecipeTypeSupported(type)) {
            context.getSource().sendMessage(Text.translatable("commands.fabricated-exchange.recipe.ban.unsupported_type", type));
            return 0;
        }
        Map<String, HashSet<String>> blacklisted = ModConfig.BLACKLISTED_MAPPER_RECIPES_FILE.getValue();
        if (!blacklisted.containsKey(type)) {
            blacklisted.put(type, new HashSet<String>());
        }

        String name = recipe.getId().toString();
        if (blacklisted.get(type).contains(name)) {
            context.getSource().sendMessage(Text.translatable("commands.fabricated-exchange.nothing"));
            return 0;
        }
        blacklisted.get(type).add(name);
        ModConfig.BLACKLISTED_MAPPER_RECIPES_FILE.save();
        context.getSource().sendMessage(Text.translatable("commands.fabricated-exchange.recipe.ban.success", type));
        return 1;
    }
    
    private static int pardonRecipe(CommandContext<ServerCommandSource> context, Recipe<?> recipe) {
        String type = recipe.getType().toString();
        if (!isRecipeTypeSupported(type)) {
            context.getSource().sendMessage(Text.translatable("commands.fabricated-exchange.recipe.pardon.unsupported_type", type));
            return 0;
        }
        
        Map<String, HashSet<String>> blacklisted = ModConfig.BLACKLISTED_MAPPER_RECIPES_FILE.getValue();
        if (!blacklisted.containsKey(type)) {
            blacklisted.put(type, new HashSet<String>());
            context.getSource().sendMessage(Text.translatable("commands.fabricated-exchange.nothing"));
            return 0;
        }
        
        String name = recipe.getId().toString();
        if (!blacklisted.get(type).contains(name)) {
            context.getSource().sendMessage(Text.translatable("commands.fabricated-exchange.nothing"));
            return 0;
        }
        blacklisted.get(type).remove(name);
        ModConfig.BLACKLISTED_MAPPER_RECIPES_FILE.save();
        context.getSource().sendMessage(Text.translatable("commands.fabricated-exchange.recipe.pardon.success", type));
        return 1;
    }

    private static int reload(CommandContext<ServerCommandSource> context) {
        MinecraftServer server = context.getSource().getServer();
        ModConfig.fetchAll();
        context.getSource().sendMessage(Text.translatable("commands.fabricated-exchange.reloademc.data_success"));
        long startTime = System.nanoTime();
        FabricatedExchange.generateBlockRotationMap(ModConfig.BLOCK_TRANSMUTATION_MAP_FILE.getValue());
        boolean hasWarned = FabricatedExchange.reloadEmcMap(server);
        FabricatedExchange.syncMaps(server);
        FabricatedExchange.syncBlockTransmutations(server);
        String add = (hasWarned)
            ? Text.translatable("commands.fabricated-exchange.reloademc.bad").getString()
            : Text.translatable("commands.fabricated-exchange.reloademc.good").getString();

        context.getSource().sendMessage(Text.translatable("commands.fabricated-exchange.reloademc.success",
        String.valueOf((System.nanoTime() - startTime) / 1000000)).append("\n").append(add));
        return 1;
    }
    private static int reset(CommandContext<ServerCommandSource> context) {
        String str = context.getArgument("datafile", String.class);
        DataFile<?> datafile = switch (str) {
            case "config" -> ModConfig.CONFIG_FILE;
            case "seed_emc_map" -> ModConfig.SEED_EMC_MAP_FILE;
            case "custom_emc_map" -> ModConfig.CUSTOM_EMC_MAP_FILE;
            default -> null;            
        };
        if (datafile == null) {
            context.getSource().sendMessage(Text.translatable("commands.fabricated-exchange.reset.no_file", str));
            return -1;
        }
        datafile.setValueToDefault();
        datafile.save();
        context.getSource().sendMessage(Text.translatable("commands.fabricated-exchange.reset.success", str));
        return 1;
    }




    private static boolean isRecipeTypeSupported(String type) {
        switch (type) {
            case "crafting":
            case "smelting":
            case "smithing":
            case "stonecutting":
                return true;
            default:
                return false;
        }
    }






}
