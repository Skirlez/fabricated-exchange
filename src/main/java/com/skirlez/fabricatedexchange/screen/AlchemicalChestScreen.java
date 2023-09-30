package com.skirlez.fabricatedexchange.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.skirlez.fabricatedexchange.FabricatedExchange;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class AlchemicalChestScreen extends HandledScreen<AlchemicalChestScreenHandler> {

	private static final Identifier TEXTURE =
			new Identifier(FabricatedExchange.MOD_ID, "textures/gui/alchemical_chest.png");

	public AlchemicalChestScreen(AlchemicalChestScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
	}

	@Override
	protected void init() {
		this.backgroundWidth = 256;
		this.backgroundHeight = 231;
		super.init();
		titleX = 0; 
		titleY = 0; 
		
	}

	@Override
	protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
		RenderSystem.setShader(GameRenderer::getPositionTexProgram);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, TEXTURE);
		int i = (width - backgroundWidth) / 2;
		int j = (height - backgroundHeight) / 2;

		this.playerInventoryTitleY = this.backgroundHeight - 93;

		drawTexture(matrices, i, j, 0, 0, backgroundWidth, backgroundHeight);
	}

	@Override
	protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {

	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
		drawMouseoverTooltip(matrices, mouseX, mouseY);
	}
}