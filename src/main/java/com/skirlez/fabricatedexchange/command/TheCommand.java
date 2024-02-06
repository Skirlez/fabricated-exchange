package com.skirlez.fabricatedexchange.command;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
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
import com.skirlez.fabricatedexchange.util.GeneralUtil;
import com.skirlez.fabricatedexchange.util.SuperNumber;
import com.skirlez.fabricatedexchange.util.config.EmcMapFile;
import com.skirlez.fabricatedexchange.util.config.ModDataFiles;
import com.skirlez.fabricatedexchange.util.config.lib.AbstractFile;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

public class TheCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralCommandNode<ServerCommandSource> mainNode = CommandManager
		// consider "/fabex", "/fabrex" if /fe is taken
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
		.executes(context -> changeRecipeStatus(context, IdentifierArgumentType.getRecipeArgument(context, "recipe"), true)))
		.build();

		LiteralCommandNode<ServerCommandSource> pardonRecipeNode = CommandManager
		.literal("pardon")
		.then(CommandManager.argument("recipe", IdentifierArgumentType.identifier()).suggests(SuggestionProviders.ALL_RECIPES)
		.executes(context -> changeRecipeStatus(context, IdentifierArgumentType.getRecipeArgument(context, "recipe"), false)))
		.build();
		
		LiteralCommandNode<ServerCommandSource> reloadNode = CommandManager
		.literal("reload")
		.requires(source -> source.hasPermissionLevel(2))
		.executes(context -> reload(context))
		.build();
		
        LiteralCommandNode<ServerCommandSource> printMissingNode = CommandManager
        .literal("printmissing")
        .executes(context -> printMissing(context))
        .build();

		LiteralCommandNode<ServerCommandSource> resetNode = CommandManager
		.literal("reset")
		.requires(source -> source.hasPermissionLevel(2))
		.then(CommandManager.argument("datafile", StringArgumentType.word()).suggests(new SuggestionProvider<ServerCommandSource>() {
			@Override
			public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context,
					SuggestionsBuilder builder) throws CommandSyntaxException {
				for (AbstractFile<?> file : ModDataFiles.ALL_FILES)
					builder.suggest(file.getName());
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
		
		mainNode.addChild(printMissingNode);

		mainNode.addChild(resetNode);

	}


	private static int help(CommandContext<ServerCommandSource> context) {
		context.getSource().sendMessage((GeneralUtil.combineTextList(GeneralUtil.translatableList("commands.fabricated-exchange.help"), "\n")));
		return 1;	
	}

	private static int setEmc(CommandContext<ServerCommandSource> context, boolean seed) {
		PlayerEntity p = context.getSource().getPlayer();
		String str = context.getArgument("number", String.class);

		if (!SuperNumber.isValidNumberString(str)) {
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
		
		String confuseMessage = (seed) ? "commands.fabricated-exchange.removeemc.seed_confused" : "commands.fabricated-exchange.removeemc.custom_confused";
		String successMessage = (seed) ? "commands.fabricated-exchange.removeemc.seed_success" : "commands.fabricated-exchange.removeemc.custom_success";
		EmcMapFile file = (seed) ? ModDataFiles.SEED_EMC_MAP : ModDataFiles.CUSTOM_EMC_MAP;
		
		if (!file.hasItem(item)) {
			context.getSource().sendMessage(Text.translatable(confuseMessage)
				.append(" ").append(Text.translatable("commands.fabricated-exchange.zero_notice")));
			return 0;
		}
		EmcData.removeItemEmc(item, seed);
		context.getSource().sendMessage(Text.translatable(successMessage)
			.append(" ").append(Text.translatable("commands.fabricated-exchange.reload_notice")));


		return 1;
	}

	private static int changeRecipeStatus(CommandContext<ServerCommandSource> context, Recipe<?> recipe, boolean ban) {
		String actionType = (ban) ? "ban" : "pardon";
		
		String unsupportedKey = "commands.fabricated-exchange.recipe." + actionType + ".unsupported_type";
		String successKey = "commands.fabricated-exchange.recipe." + actionType + ".success";
		
		String type = recipe.getType().toString();
		if (!isRecipeTypeSupported(type)) {
			context.getSource().sendMessage(Text.translatable(unsupportedKey, type));
			return 0;
		}
		
		String name = recipe.getId().toString();
		boolean alreadyBlacklisted = ModDataFiles.BLACKLISTED_MAPPER_RECIPES.isRecipeBlacklisted(name, type);
		if (alreadyBlacklisted == ban) { // (alreadyBlacklisted && ban) || (!alreadyBlacklisted && !ban)
			context.getSource().sendMessage(Text.translatable("commands.fabricated-exchange.nothing"));
			return 0;
		}
		Map<String, HashSet<String>> blacklisted = ModDataFiles.BLACKLISTED_MAPPER_RECIPES.getCopy();
		if (ban)
			blacklisted.get(type).add(name);
		else
			blacklisted.get(type).remove(name);
		ModDataFiles.BLACKLISTED_MAPPER_RECIPES.setValueAndSave(blacklisted);
		context.getSource().sendMessage(Text.translatable(successKey, type));
		return 1;
	}
	

	private static int reload(CommandContext<ServerCommandSource> context) {
		FabricatedExchange.reload();
		
		context.getSource().sendMessage(Text.translatable("commands.fabricated-exchange.reloademc.data_success"));
		
		return reloadEmcMap(context);
	}
	
	private static int reloadEmcMap(CommandContext<ServerCommandSource> context) {
		MinecraftServer server = context.getSource().getServer();
		long startTime = System.nanoTime();
		boolean hasWarned = FabricatedExchange.calculateEmcMap(server);

		String add = (hasWarned)
			? Text.translatable("commands.fabricated-exchange.reloademc.bad").getString()
			: Text.translatable("commands.fabricated-exchange.reloademc.good").getString();
		
		FabricatedExchange.syncMaps(server);
		FabricatedExchange.syncBlockTransmutations(server);
		
		context.getSource().sendMessage(Text.translatable("commands.fabricated-exchange.reloademc.success",
		String.valueOf((System.nanoTime() - startTime) / 1000000)).append("\n").append(add));
		return 1;	
	}
	
	
	private static int reset(CommandContext<ServerCommandSource> context) {
		String str = context.getArgument("datafile", String.class);
		Optional<AbstractFile<?>> maybeDatafile = ModDataFiles.getFileByName(str);
		if (maybeDatafile.isEmpty()) {
			context.getSource().sendMessage(Text.translatable("commands.fabricated-exchange.reset.no_file", str));
			return -1;
		}
		AbstractFile<?> datafile = maybeDatafile.get();
		datafile.setValueToDefault();
		datafile.save();
		context.getSource().sendMessage(Text.translatable("commands.fabricated-exchange.reset.success", str));
		return 1;
	}

    private static int printMissing(CommandContext<ServerCommandSource> context) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Item item : Registries.ITEM) {
            if (EmcData.getItemEmc(item).equalsZero()) {
                stringBuilder.append('\n');
                stringBuilder.append(Registries.ITEM.getId(item));
            }
        }
        if (stringBuilder.isEmpty()) {
            context.getSource().sendMessage(Text.of("Every item has an EMC value!"));
        }
        else {
            stringBuilder.delete(0, 1); // remove newline at the start
            context.getSource().sendMessage(Text.of(stringBuilder.toString()));
        }
        return 1;    
    }


	private static boolean isRecipeTypeSupported(String type) {
		switch (type) {
			case "crafting":
			case "smelting":
			case "smithing":
			case "brewing":
			case "stonecutting":
				return true;
			default:
				return false;
		}
	}
}
