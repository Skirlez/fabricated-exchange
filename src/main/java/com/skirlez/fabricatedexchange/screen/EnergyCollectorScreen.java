package com.skirlez.fabricatedexchange.screen;


import com.mojang.blaze3d.systems.RenderSystem;
import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.emc.EmcData;
import com.skirlez.fabricatedexchange.util.SuperNumber;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import com.skirlez.fabricatedexchange.screen.EnergyCollectorScreenHandler.SlotIndicies;

public class EnergyCollectorScreen extends HandledScreen<EnergyCollectorScreenHandler> {
	private final Identifier texture;
	private double emcProgress; // 0 - 1 how much of the emc bar should be filled
	private final EnergyCollectorScreenHandler handler;
	private int light;
	private long emc;
	private final int level;
	private final long maximumEmc;
	public EnergyCollectorScreen(EnergyCollectorScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);		
		
		this.level = handler.getLevel();
		if (this.level == 0)
			maximumEmc = 10000;
		else if (this.level == 1)
			maximumEmc = 30000;
		else 
			maximumEmc = 60000;

		this.handler = handler;
		PacketByteBuf buf = handler.getAndConsumeCreationBuffer().get();
		emc = Long.parseLong(buf.readString());
		light = buf.readInt();


		emcProgress = ((double)emc / (double)maximumEmc);
		

		texture = new Identifier(FabricatedExchange.MOD_ID, "textures/gui/energy_collector_mk" + String.valueOf(level + 1) + ".png");
	}

	@Override
	protected void init() {
		int xOffset;
		if (level == 0)
			xOffset = 0;
		else if (level == 1)
			xOffset = 25;
		else
			xOffset = 43;
		this.backgroundWidth = 176 + xOffset;
		this.backgroundHeight = 166;
		super.init();
		titleX = 0; 
		titleY = 0; 
		
	}

	@Override
	protected void handledScreenTick() {
		emcProgress = ((double)emc / (double)maximumEmc);
	}

	@Override
	protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
		RenderSystem.setShader(GameRenderer::getPositionTexProgram);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, texture);
		int x = (width - backgroundWidth) / 2;
		int y = (height - backgroundHeight) / 2;
		drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight);


		int xOffset = 0; 
		int uOffset = 0;
		if (this.level == 1) {
			xOffset = 16;
			uOffset = 25;
		}
		else if (this.level == 2) {
			xOffset = 34;
			uOffset = 43;
		}

		// arrowOffset and sunOffset are for drawing the sun and the arrow sliced, 
		// xOffset and uOffset are for offsetting the elements it draws correctly on all sizes.
		double fuelProgress = calculateFuelProgress();
		
		final int maxfuelProgress = 24;
		int arrowOffset = maxfuelProgress - (int)(fuelProgress * maxfuelProgress);
		drawTexture(matrices, x + 138 + xOffset, y + 31 + arrowOffset, 176 + uOffset, 14 + arrowOffset, 12, 25); // draw arrow

		int sunOffset = (int)(((float)light) / 15 * 12);
		drawTexture(matrices, x + 126 + xOffset, y + 49 - sunOffset, 177 + uOffset, 12 - sunOffset, 12, sunOffset); // draw sun

		drawTexture(matrices, x + 64 + xOffset, y + 18, 0, 166, (int)(48 * emcProgress), 10); // draw energy bar thing
	}

	@Override
	protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
		int xOffset = 0; 
		if (this.level == 1)
			xOffset = 16;
		else if (this.level == 2)
			xOffset = 34;
		textRenderer.draw(matrices, Long.toString(emc), 62 + xOffset, 32, 0x404040);
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
		drawMouseoverTooltip(matrices, mouseX, mouseY);
	}


	public void update(long emc, int light) {
		this.emc = emc;
		this.light = light;
	}

	public double calculateFuelProgress() {
		Inventory inventory = handler.getInventory();
		
		ItemStack stack = inventory.getStack(SlotIndicies.FUEL_SLOT.ordinal());
		if (stack.isEmpty())
			return 0d;
		Item item = stack.getItem();
		if (!FabricatedExchange.fuelProgressionMap.containsKey(item))
			return 0d;
		SuperNumber itemEmc = EmcData.getItemEmc(item);
		
		ItemStack target = handler.getTargetItemStack();
		if (!target.isEmpty()) {
			SuperNumber targetItemEmc = EmcData.getItemEmc(target.getItem());
			if (itemEmc.compareTo(targetItemEmc) >= 0)
				return 0d;
		}

		Item nextItem = FabricatedExchange.fuelProgressionMap.get(item);
		ItemStack outputStack = inventory.getStack(SlotIndicies.OUTPUT_SLOT.ordinal());
		if ((!nextItem.equals(outputStack.getItem())
				|| outputStack.getMaxCount() <= outputStack.getCount()
				) && !outputStack.isEmpty())
			return 0d; // return if there's an item in the output slot that we cannot merge with the next item in the fuel progression
		SuperNumber nextEmc = EmcData.getItemEmc(nextItem);
		
		nextEmc.subtract(itemEmc);

		double fuelProgress = ((double)emc / nextEmc.toDouble());
		if (fuelProgress > 1d)
			fuelProgress = 1d;
		return fuelProgress;
		
	}
}
