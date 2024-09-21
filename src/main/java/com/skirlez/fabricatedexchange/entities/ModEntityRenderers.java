package com.skirlez.fabricatedexchange.entities;

import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class ModEntityRenderers {
	public static void registerEntityRenderers() {
		EntityRendererRegistry.register(ModEntities.WATER_PROJECTILE, ProjectileRenderer::new);
		EntityRendererRegistry.register(ModEntities.LAVA_PROJECTILE, ProjectileRenderer::new);
		EntityRendererRegistry.register(ModEntities.FUNCTIONAL_PROJECTILE, ProjectileRenderer::new);
	}
}
