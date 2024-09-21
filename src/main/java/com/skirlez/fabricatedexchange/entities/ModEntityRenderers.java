package com.skirlez.fabricatedexchange.entities;

import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public final class ModEntityRenderers {
	private ModEntityRenderers() {}

	public static void register() {
		EntityRendererRegistry.register(ModEntities.WATER_PROJECTILE, ProjectileRenderer::new);
		EntityRendererRegistry.register(ModEntities.LAVA_PROJECTILE, ProjectileRenderer::new);
		EntityRendererRegistry.register(ModEntities.FUNCTIONAL_PROJECTILE, ProjectileRenderer::new);
	}
}
