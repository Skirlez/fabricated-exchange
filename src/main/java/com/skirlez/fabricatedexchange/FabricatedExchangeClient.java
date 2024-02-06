package com.skirlez.fabricatedexchange;
import com.skirlez.fabricatedexchange.block.AlchemicalChestBlockEntity;
import com.skirlez.fabricatedexchange.block.EnergyCondenserBlockEntity;
import com.skirlez.fabricatedexchange.block.ModBlockEntities;
import com.skirlez.fabricatedexchange.block.ModBlocks;
import com.skirlez.fabricatedexchange.event.KeyInputHandler;
import com.skirlez.fabricatedexchange.packets.ModServerToClientPackets;
import com.skirlez.fabricatedexchange.screen.ModScreens;
import com.skirlez.fabricatedexchange.util.SuperNumber;
import com.skirlez.fabricatedexchange.util.config.ModDataFiles;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.block.entity.ChestBlockEntityRenderer;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
public class FabricatedExchangeClient implements ClientModInitializer {
	
	public static SuperNumber clientEmc = SuperNumber.Zero();

	private static final BlockEntity RENDER_ALCHEMICAL_CHEST = 
		new AlchemicalChestBlockEntity(BlockPos.ORIGIN, ModBlocks.ALCHEMICAL_CHEST.getDefaultState());
	private static final BlockEntity RENDER_ENERGY_CONDENSER_MK1 = 
		new EnergyCondenserBlockEntity(BlockPos.ORIGIN, ModBlocks.ENERGY_CONDENSER_MK1.getDefaultState());
	private static final BlockEntity RENDER_ENERGY_CONDENSER_MK2 = 
		new EnergyCondenserBlockEntity(BlockPos.ORIGIN, ModBlocks.ENERGY_CONDENSER_MK2.getDefaultState());

	public static boolean hasInitiallyLoadedConfig = false;
	
	@Override
	public void onInitializeClient() {
		ModScreens.register();
		KeyInputHandler.register();
		ModServerToClientPackets.register();
		
		
		// we can use the vanilla renderers due to the texture mixin, see ModTexturedRenderLayers
		BlockEntityRendererFactories.register(ModBlockEntities.ALCHEMICAL_CHEST, ChestBlockEntityRenderer::new);
		BlockEntityRendererFactories.register(ModBlockEntities.ENERGY_CONDENSER, ChestBlockEntityRenderer::new);

		BuiltinItemRendererRegistry.INSTANCE.register(ModBlocks.ALCHEMICAL_CHEST.asItem(), (stack, mode, matrices, vertexConsumers, light, overlay)
			-> MinecraftClient.getInstance().getBlockEntityRenderDispatcher().renderEntity(RENDER_ALCHEMICAL_CHEST, matrices, vertexConsumers, light, overlay));
		BuiltinItemRendererRegistry.INSTANCE.register(ModBlocks.ENERGY_CONDENSER_MK1.asItem(), (stack, mode, matrices, vertexConsumers, light, overlay) 
			-> MinecraftClient.getInstance().getBlockEntityRenderDispatcher().renderEntity(RENDER_ENERGY_CONDENSER_MK1, matrices, vertexConsumers, light, overlay));
		BuiltinItemRendererRegistry.INSTANCE.register(ModBlocks.ENERGY_CONDENSER_MK2.asItem(), (stack, mode, matrices, vertexConsumers, light, overlay) 
			-> MinecraftClient.getInstance().getBlockEntityRenderDispatcher().renderEntity(RENDER_ENERGY_CONDENSER_MK2, matrices, vertexConsumers, light, overlay));

		/*
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			//if (!MinecraftClient.getInstance().isIntegratedServerRunning())
				
		});
		*/
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
				@Override
				public Identifier getFabricId() {
					return new Identifier(FabricatedExchange.MOD_ID, "load_config");
				}
				@Override
				public void reload(ResourceManager resourceManager) {
					ModDataFiles.MAIN_CONFIG_FILE.updateComments();
					
					// Dirty hack so the config file is loaded on startup after the translation keys are ready 
					// (for comments)
					if (!hasInitiallyLoadedConfig) {
						ModDataFiles.MAIN_CONFIG_FILE.fetch();
						hasInitiallyLoadedConfig = true;
					}
					
				}
			});

	}
}
