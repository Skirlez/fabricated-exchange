/*
 * Decompiled with CFR 0.2.1 (FabricMC 53fa44c9).
 */
package com.skirlez.fabricatedexchange.entities;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;


// FlyingItemEntityRenderer with one portion commented out
@Environment(EnvType.CLIENT)
public class ProjectileRenderer<T extends Entity> extends EntityRenderer<T> {
	private final ItemRenderer itemRenderer;
	private final float scale;
	private final boolean lit;

	public ProjectileRenderer(EntityRendererFactory.Context ctx, float scale, boolean lit) {
		super(ctx);
		this.itemRenderer = ctx.getItemRenderer();
		this.scale = scale;
		this.lit = lit;
	}

	public ProjectileRenderer(EntityRendererFactory.Context context) {
		this(context, 1.0f, false);
	}

	@Override
	protected int getBlockLight(T entity, BlockPos pos) {
		return this.lit ? 15 : super.getBlockLight(entity, pos);
	}

	@Override
	public void render(T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
		/*
		if (((Entity)entity).age < 2 && this.dispatcher.camera.getFocusedEntity().squaredDistanceTo((Entity)entity) < 12.25) {
			return;
		}
		*/
		matrices.push();
		matrices.scale(this.scale, this.scale, this.scale);
		matrices.multiply(this.dispatcher.getRotation());
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f));
		this.itemRenderer.renderItem(((FlyingItemEntity)entity).getStack(), ModelTransformationMode.GROUND, light, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, ((Entity)entity).world, ((Entity)entity).getId());
		matrices.pop();
		super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
	}

	@Override
	public Identifier getTexture(Entity entity) {
		return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
	}
}

