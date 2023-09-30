package com.skirlez.fabricatedexchange.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.emc.EmcData;
import com.skirlez.fabricatedexchange.util.SuperNumber;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class EnergyCondenserScreen extends HandledScreen<EnergyCondenserScreenHandler> {

	private final int level;

	private final Identifier texture;
	private SuperNumber emc;

	public EnergyCondenserScreen(EnergyCondenserScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
		this.level = handler.getLevel();
		PacketByteBuf buf = handler.getAndConsumeCreationBuffer();
		if (buf == null)
			emc = SuperNumber.Zero();
		else
			emc = new SuperNumber(buf.readString());
		this.texture = new Identifier(FabricatedExchange.MOD_ID, "textures/gui/energy_condenser_mk" + (level + 1) + ".png");
	}

	@Override
	protected void init() {
		this.backgroundWidth = 256;
		this.backgroundHeight = 234;
		super.init();
		titleX = 0; 
		titleY = 0; 
	}

	@Override
	protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
		RenderSystem.setShader(GameRenderer::getPositionTexProgram);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, texture);
		int i = (width - backgroundWidth) / 2;
		int j = (height - backgroundHeight) / 2;

		this.playerInventoryTitleY = this.backgroundHeight - 93;

		drawTexture(matrices, i, j, 0, 0, backgroundWidth, backgroundHeight);
		
		
		ItemStack stack = handler.getInventory().getStack(0);
		if (stack.isEmpty())
			return;
		SuperNumber targetEmc = EmcData.getItemStackEmc(stack);
		if (targetEmc.equalsZero())
			return;
		SuperNumber emcCopy = new SuperNumber(emc);
		emcCopy.divide(targetEmc);
		double percent = emcCopy.toDouble();
		if (percent > 1d)
			percent = 1d;
		drawTexture(matrices, x + 33, y + 9, 0, backgroundHeight, (int)(102 * percent), 10);
	}

	@Override
	protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
		ItemStack stack = handler.getInventory().getStack(0);
		SuperNumber targetEmc;
		if (stack.isEmpty())
			targetEmc = emc;
		targetEmc = EmcData.getItemStackEmc(stack);
		SuperNumber emcCopy = new SuperNumber(SuperNumber.min(emc, targetEmc));
		emcCopy.floor();

		textRenderer.draw(matrices, emcCopy.toString(), 150, 10, 0x404040);
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
		drawMouseoverTooltip(matrices, mouseX, mouseY);
	}

	public void update(SuperNumber emc) {
		this.emc = emc;
	}
}