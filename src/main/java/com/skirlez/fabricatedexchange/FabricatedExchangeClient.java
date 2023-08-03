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
import com.skirlez.fabricatedexchange.util.ModConfig;
import com.skirlez.fabricatedexchange.util.SuperNumber;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.block.entity.ChestBlockEntityRenderer;
import net.minecraft.util.math.BlockPos;
public class FabricatedExchangeClient implements ClientModInitializer {
    
    public static SuperNumber clientEmc = SuperNumber.Zero();

    private static final BlockEntity renderAlchemicalChest = 
        new AlchemicalChestBlockEntity(BlockPos.ORIGIN, ModBlocks.ALCHEMICAL_CHEST.getDefaultState());
    private static final BlockEntity renderEnergyCollectorMk1 = 
        new EnergyCondenserBlockEntity(BlockPos.ORIGIN, ModBlocks.ENERGY_CONDENSER_MK1.getDefaultState());
    private static final BlockEntity renderEnergyCollectorMk2 = 
        new EnergyCondenserBlockEntity(BlockPos.ORIGIN, ModBlocks.ENERGY_CONDENSER_MK2.getDefaultState());

    @Override
    public void onInitializeClient() {
        HandledScreens.register(ModScreenHandlers.TRANSMUTATION_TABLE_SCREEN_HANDLER, TransmutationTableScreen::new);
        HandledScreens.register(ModScreenHandlers.ENERGY_COLLECTOR_SCREEN_HANDLER, EnergyCollectorScreen::new);
        HandledScreens.register(ModScreenHandlers.ANTIMATTER_RELAY_SCREEN_HANDLER, AntiMatterRelayScreen::new);
        HandledScreens.register(ModScreenHandlers.ALCHEMICAL_CHEST_SCREEN_HANDLER, AlchemicalChestScreen::new);
        HandledScreens.register(ModScreenHandlers.ENERGY_CONDENSER_SCREEN_HANDLER, EnergyCondenserScreen::new);
        KeyInputHandler.register();
        ModMessages.registerS2CPackets();
        
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            ClientCommand.register(dispatcher);
        });
        // we can use the vanilla renderers due to the texture mixin, see ModTexturedRenderLayers
        BlockEntityRendererFactories.register(ModBlockEntities.ALCHEMICAL_CHEST, ChestBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(ModBlockEntities.ENERGY_CONDENSER, ChestBlockEntityRenderer::new);

        BuiltinItemRendererRegistry.INSTANCE.register(ModBlocks.ALCHEMICAL_CHEST.asItem(), (stack, mode, matrices, vertexConsumers, light, overlay) 
            -> MinecraftClient.getInstance().getBlockEntityRenderDispatcher().renderEntity(renderAlchemicalChest, matrices, vertexConsumers, light, overlay));
        BuiltinItemRendererRegistry.INSTANCE.register(ModBlocks.ENERGY_CONDENSER_MK1.asItem(), (stack, mode, matrices, vertexConsumers, light, overlay) 
            -> MinecraftClient.getInstance().getBlockEntityRenderDispatcher().renderEntity(renderEnergyCollectorMk1, matrices, vertexConsumers, light, overlay));
        BuiltinItemRendererRegistry.INSTANCE.register(ModBlocks.ENERGY_CONDENSER_MK2.asItem(), (stack, mode, matrices, vertexConsumers, light, overlay) 
            -> MinecraftClient.getInstance().getBlockEntityRenderDispatcher().renderEntity(renderEnergyCollectorMk2, matrices, vertexConsumers, light, overlay));

        ModConfig.fetchAll();
    }



}
