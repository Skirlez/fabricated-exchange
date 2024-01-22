package com.skirlez.fabricatedexchange;
import com.skirlez.fabricatedexchange.block.AlchemicalChestBlockEntity;
import com.skirlez.fabricatedexchange.block.EnergyCondenserBlockEntity;
import com.skirlez.fabricatedexchange.block.ModBlockEntities;
import com.skirlez.fabricatedexchange.block.ModBlocks;
import com.skirlez.fabricatedexchange.command.ClientCommand;
import com.skirlez.fabricatedexchange.event.KeyInputHandler;
import com.skirlez.fabricatedexchange.networking.ModMessages;
import com.skirlez.fabricatedexchange.screen.AlchemicalChestScreen;
import com.skirlez.fabricatedexchange.screen.AntiMatterRelayScreen;
import com.skirlez.fabricatedexchange.screen.EnergyCollectorScreen;
import com.skirlez.fabricatedexchange.screen.EnergyCondenserScreen;
import com.skirlez.fabricatedexchange.screen.ModScreenHandlers;
import com.skirlez.fabricatedexchange.screen.TransmutationTableScreen;
import com.skirlez.fabricatedexchange.util.SuperNumber;
import com.skirlez.fabricatedexchange.util.config.ModDataFiles;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.block.entity.ChestBlockEntityRenderer;
import net.minecraft.util.math.BlockPos;
public class FabricatedExchangeClient implements ClientModInitializer {
	
	public static SuperNumber clientEmc = SuperNumber.Zero();

	private static final BlockEntity RENDER_ALCHEMICAL_CHEST = 
		new AlchemicalChestBlockEntity(BlockPos.ORIGIN, ModBlocks.ALCHEMICAL_CHEST.getDefaultState());
	private static final BlockEntity RENDER_ENERGY_CONDENSER_MK1 = 
		new EnergyCondenserBlockEntity(BlockPos.ORIGIN, ModBlocks.ENERGY_CONDENSER_MK1.getDefaultState());
	private static final BlockEntity RENDER_ENERGY_CONDENSER_MK2 = 
		new EnergyCondenserBlockEntity(BlockPos.ORIGIN, ModBlocks.ENERGY_CONDENSER_MK2.getDefaultState());

	@Override
	public void onInitializeClient() {
		HandledScreens.register(ModScreenHandlers.TRANSMUTATION_TABLE, TransmutationTableScreen::new);
		HandledScreens.register(ModScreenHandlers.ENERGY_COLLECTOR, EnergyCollectorScreen::new);
		HandledScreens.register(ModScreenHandlers.ANTIMATTER_RELAY, AntiMatterRelayScreen::new);
		HandledScreens.register(ModScreenHandlers.ALCHEMICAL_CHEST, AlchemicalChestScreen::new);
		HandledScreens.register(ModScreenHandlers.ENERGY_CONDENSER, EnergyCondenserScreen::new);
		KeyInputHandler.register();
		ModMessages.registerS2CPackets();
		
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			ClientCommand.register(dispatcher);
		});
		
		// we can use the vanilla renderers due to the texture mixin, see ModTexturedRenderLayers
		BlockEntityRendererFactories.register(ModBlockEntities.ALCHEMICAL_CHEST, ChestBlockEntityRenderer::new);
		BlockEntityRendererFactories.register(ModBlockEntities.ENERGY_CONDENSER, ChestBlockEntityRenderer::new);

		BuiltinItemRendererRegistry.INSTANCE.register(ModBlocks.ALCHEMICAL_CHEST.asItem(), (stack, mode, matrices, vertexConsumers, light, overlay)
			-> MinecraftClient.getInstance().getBlockEntityRenderDispatcher().renderEntity(RENDER_ALCHEMICAL_CHEST, matrices, vertexConsumers, light, overlay));
		BuiltinItemRendererRegistry.INSTANCE.register(ModBlocks.ENERGY_CONDENSER_MK1.asItem(), (stack, mode, matrices, vertexConsumers, light, overlay) 
			-> MinecraftClient.getInstance().getBlockEntityRenderDispatcher().renderEntity(RENDER_ENERGY_CONDENSER_MK1, matrices, vertexConsumers, light, overlay));
		BuiltinItemRendererRegistry.INSTANCE.register(ModBlocks.ENERGY_CONDENSER_MK2.asItem(), (stack, mode, matrices, vertexConsumers, light, overlay) 
			-> MinecraftClient.getInstance().getBlockEntityRenderDispatcher().renderEntity(RENDER_ENERGY_CONDENSER_MK2, matrices, vertexConsumers, light, overlay));

		
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			if (!MinecraftClient.getInstance().isIntegratedServerRunning())
				ModDataFiles.fetchAll();
		});
	}



}
