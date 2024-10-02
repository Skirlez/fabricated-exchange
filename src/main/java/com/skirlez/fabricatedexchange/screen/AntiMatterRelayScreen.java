package com.skirlez.fabricatedexchange.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.screen.AntiMatterRelayScreenHandler.SlotIndicies;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.text.NumberFormat;

public class AntiMatterRelayScreen extends HandledScreen<AntiMatterRelayScreenHandler> {
	private final Identifier texture;
	private final int level;
	private long emc;
	private final long maximumEmc;
	private final AntiMatterRelayScreenHandler handler;
	public AntiMatterRelayScreen(AntiMatterRelayScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
		this.handler = handler;
		this.level = handler.getLevel();
		PacketByteBuf buf = handler.getAndConsumeCreationBuffer().get();
		this.emc = Long.parseLong(buf.readString());

		if (level == 0) 
			maximumEmc = 100000;
		else if (level == 1) 
			maximumEmc = 1000000;
		else 
			maximumEmc = 10000000;
		
		texture = new Identifier(FabricatedExchange.MOD_ID, 
			"textures/gui/antimatter_relay_mk" + String.valueOf(level + 1) + ".png");
	}

	@Override
	protected void init() {
		int xOffset, yOffset;
		if (level == 0) {
			xOffset = 0;
			yOffset = 0;
		}
		else if (level == 1) {
			xOffset = 18;
			yOffset = 6;
		}
		else {
			xOffset = 37;
			yOffset = 18;
		}
		this.backgroundWidth = 176 + xOffset;
		this.backgroundHeight = 177 + yOffset;
		super.init();
		titleX = 0; 
		titleY = 0; 
		
	}

	@Override
	protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
		RenderSystem.setShader(GameRenderer::getPositionTexProgram);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, texture);
		int x = (width - backgroundWidth) / 2;
		int y = (height - backgroundHeight) / 2 - 5;
		drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight);

		

		int xFuel, yFuel, xBar;
		if (level == 0) {
			xBar = 0;
			xFuel = 0;
			yFuel = 0;
		}
		else if (level == 1) {
			xBar = 22;
			xFuel = 17;
			yFuel = 1;
		}
		else {
			xBar = 41;
			xFuel = 37;
			yFuel = 15;
		}

		double percent;
		
		// draw fuel energy bar 
		ItemStack fuelStack = handler.getInventory().getStack(SlotIndicies.FUEL_SLOT.ordinal());
		percent = (double)fuelStack.getCount() / fuelStack.getMaxCount();
		drawTexture(matrices, x + 64 + xFuel, y + 67 + yFuel, 0, backgroundHeight, (int)(30 * percent), 10); 


		percent = ((double)emc / (double)maximumEmc);
		if (percent > 1)
			percent = 1;
		drawTexture(matrices, x + 64 + xBar, y + 6, 30, backgroundHeight, (int)(102 * percent), 10); 
		
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
		drawMouseoverTooltip(matrices, mouseX, mouseY);
	}

	@Override
	protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
		int xOffset;
		if (level == 0) 
			xOffset = 0;
		else if (level == 1) 
			xOffset = 17;
		else 
			xOffset = 37;
		

		textRenderer.draw(matrices, NumberFormat.getIntegerInstance().format(emc), 88 + xOffset, 16, 0x404040);
	}

	public void update(long emc) {
		this.emc = emc;
	}
}
