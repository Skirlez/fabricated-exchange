package com.skirlez.fabricatedexchange.entities;

import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;

public class ModEntityRenderers {
	public static void registerEntityRenderers() {
		EntityRendererRegistry.register(ModEntities.WATER_PROJECTILE, ProjectileRenderer::new);
		EntityRendererRegistry.register(ModEntities.LAVA_PROJECTILE, ProjectileRenderer::new);
		EntityRendererRegistry.register(ModEntities.TORNADO_PROJECTILE, ProjectileRenderer::new);
		EntityRendererRegistry.register(ModEntities.FROZEN_PROJECTILE, ProjectileRenderer::new);
	}
}
