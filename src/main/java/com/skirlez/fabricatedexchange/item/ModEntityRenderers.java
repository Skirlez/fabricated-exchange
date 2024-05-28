package com.skirlez.fabricatedexchange.item;

import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;

public class ModEntityRenderers {
	public static void registerEntityRenderers() {
		EntityRendererRegistry.register(ModEntities.WATER_PROJECTILE, FlyingItemEntityRenderer::new);
		EntityRendererRegistry.register(ModEntities.LAVA_PROJECTILE, FlyingItemEntityRenderer::new);
		EntityRendererRegistry.register(ModEntities.TORNADO_PROJECTILE, FlyingItemEntityRenderer::new);
		EntityRendererRegistry.register(ModEntities.FROZEN_PROJECTILE, FlyingItemEntityRenderer::new);
	}
}
