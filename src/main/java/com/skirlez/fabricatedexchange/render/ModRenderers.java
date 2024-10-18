package com.skirlez.fabricatedexchange.render;

import com.skirlez.fabricatedexchange.entities.ModEntities;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public final class ModRenderers {
	private ModRenderers() {};

	public static void register() {
		MercurialEyeOutlineRenderer.register();
		OutliningItemRenderer.register();

		EntityRendererRegistry.register(ModEntities.WATER_PROJECTILE, ProjectileRenderer::new);
		EntityRendererRegistry.register(ModEntities.LAVA_PROJECTILE, ProjectileRenderer::new);
		EntityRendererRegistry.register(ModEntities.FUNCTIONAL_PROJECTILE, ProjectileRenderer::new);
	}
}
