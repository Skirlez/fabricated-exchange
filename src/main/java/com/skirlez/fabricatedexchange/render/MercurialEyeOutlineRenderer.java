package com.skirlez.fabricatedexchange.render;

import com.skirlez.fabricatedexchange.item.tools.MercurialEye;
import com.skirlez.fabricatedexchange.util.GeneralUtil;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;

public final class MercurialEyeOutlineRenderer {
	private MercurialEyeOutlineRenderer() {};

	public static void register() {
		WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register(((context, hitResult) -> {
			render(context);
			return true;
		}));
	}


	// List of edges.
	// The first 3 values are the x, y, z of one point, and the next 3 are the x, y, z of another.
	// Connecting each element's 2 points gives you all the edges in a unit cube.
	private static final int[][] edges = new int[][] {
		{0, 0, 0, 0, 0, 1},
		{0, 0, 0, 0, 1, 0},
		{0, 0, 0, 1, 0, 0},

		{0, 1, 0, 0, 1, 1},
		{0, 1, 0, 1, 1, 0},

		{1, 1, 0, 1, 1, 1},
		{1, 1, 0, 1, 0, 0},

		{1, 0, 0, 1, 0, 1},

		{0, 0, 1, 0, 1, 1},
		{0, 0, 1, 1, 0, 1},

		{1, 0, 1, 1, 1, 1},

		{0, 1, 1, 1, 1, 1},
	};

	private static void render(WorldRenderContext context) {
		MinecraftClient client = MinecraftClient.getInstance();
		ItemStack stack = client.player.getStackInHand(Hand.MAIN_HAND);
		if (!(stack.getItem() instanceof MercurialEye)) {
			stack = client.player.getStackInHand(Hand.OFF_HAND);
			if (!(stack.getItem() instanceof MercurialEye))
				return;
		}
		BlockPos[] positions = MercurialEye.getPlayerBlocks(client.player);
		ClientWorld world = context.world();


		Vec3d pos1;
		Vec3d pos2;

		Vec3d center1;
		Vec3d center2;

		if (positions.length == 1) {
			Vec3d center = positions[0].toCenterPos();
			pos1 = center.add(0.5, 0.5, 0.5);
			pos2 = center.subtract(0.5, 0.5, 0.5);
			center1 = center;
			center2 = center;
		}
		else if (positions.length == 2) {
			center1 = positions[0].toCenterPos();
			center2 = positions[1].toCenterPos();

			pos1 = GeneralUtil.farthestCubeVertexFromPoint(center1, center2);
			pos2 = GeneralUtil.farthestCubeVertexFromPoint(center2, pos1);
		}
		else
			return;

		MatrixStack matrices = context.matrixStack();
		matrices.push();
		VertexConsumerProvider provider = context.consumers();
		VertexConsumer vertexConsumer = provider.getBuffer(RenderLayer.LINES);
		Vec3d cameraPos = context.camera().getPos();
		double cameraX = cameraPos.getX();
		double cameraY = cameraPos.getY();
		double cameraZ = cameraPos.getZ();

		double offsetX = -cameraX;
		double offsetY = -cameraY;
		double offsetZ = -cameraZ;
		MatrixStack.Entry entry = matrices.peek();

		double[] ones = new double[] { pos1.x, pos1.y, pos1.z };
		double[] twos = new double[] { pos2.x, pos2.y, pos2.z };
		double points[][] = new double[][] {ones, twos};

		for (int[] edge : edges) {
			double x1 = points[edge[0]][0];
			double y1 = points[edge[1]][1];
			double z1 = points[edge[2]][2];

			double x2 = points[edge[3]][0];
			double y2 = points[edge[4]][1];
			double z2 = points[edge[5]][2];

			float k = (float)(x2 - x1);
			float l = (float)(y2 - y1);
			float m = (float)(z2 - z1);
			float n = MathHelper.sqrt(k * k + l * l + m * m);
			vertexConsumer.vertex(entry.getPositionMatrix(),
				(float)(x1 - cameraX), (float)(y1 - cameraY), (float)(z1 - cameraZ))
				.color(0f, 0f, 0f, 0.4f)
				.normal(entry.getNormalMatrix(), k /= n, l /= n, m /= n)
				.next();

			vertexConsumer.vertex(entry.getPositionMatrix(),
				(float)(x2 - cameraX), (float)(y2 - cameraY), (float)(z2 - cameraZ))
				.color(0f, 0f, 0f, 0.4f)
				.normal(entry.getNormalMatrix(), k, l, m)
				.next();
		}
		matrices.pop();

		drawText("➀", center1, matrices, provider, context.camera());
		if (positions.length == 2 && !center1.equals(center2))
			drawText("➁", center2, matrices, provider, context.camera());
	}


	private static void drawText(String text, Vec3d pos, MatrixStack matrices, VertexConsumerProvider provider, Camera camera) {
		matrices.push();
		matrices.translate(pos.x - camera.getPos().x, pos.y - camera.getPos().y, pos.z - camera.getPos().z);
		matrices.multiply(new Quaternionf(camera.getRotation())
			.rotateX((float)Math.toRadians(180))
			.rotateZ((float)Math.toRadians(180)));

		float f = 0.015625f * 2;
		matrices.scale(f, -f, f);
		int i = MathHelper.packRgb(1, 1, 1);
		int j = 0;

		MinecraftClient client = MinecraftClient.getInstance();
		client.textRenderer.draw(text,
			-(float)client.textRenderer.getWidth(text) / 2f, -4,
			MathHelper.packRgb(1, 1, 1), false,
			matrices.peek().getPositionMatrix(), provider,
			TextRenderer.TextLayerType.SEE_THROUGH,
			0, 255);
		client.textRenderer.draw(text,
			-(float)client.textRenderer.getWidth(text) / 2f, -4,
			MathHelper.packRgb(1, 1, 1), false,
			matrices.peek().getPositionMatrix(), provider,
			TextRenderer.TextLayerType.NORMAL,
			0, 255);
		matrices.pop();
	}
}
