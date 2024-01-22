package com.skirlez.fabricatedexchange.screen;

import java.util.Map;

import com.skirlez.fabricatedexchange.util.config.ModDataFiles;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class SettingsScreen extends GameOptionsScreen {
	private final Map<String, Object> CONFIG;
	private final Map<String, String[]> COMMENTS;
	private int lastY;
	private int distFromLeft;
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	public SettingsScreen(Screen previous) {
		super(previous, CLIENT.options, Text.of("Fabricated Exchange"));
		CONFIG = ModDataFiles.MAIN_CONFIG_FILE.getCopy();
		COMMENTS = ModDataFiles.MAIN_CONFIG_FILE.getComments();
	}

	@Override
	protected void init() {
		super.init();
		this.lastY = 35;
		this.distFromLeft = 30;
		if (this.client.world == null || MinecraftClient.getInstance().isIntegratedServerRunning()) {
			
			Text closeWithoutSavingText = Text.of("Close Without Saving");
			ButtonWidget closeWithoutSaving = new ButtonWidget.Builder(closeWithoutSavingText, (widget) -> {
				close();
			}).dimensions(distFromLeft, this.height - 30, textRenderer.getWidth(closeWithoutSavingText) + 20, 20).build();
			
			addDrawableChild(closeWithoutSaving);
			
			Text doneText = Text.of("Done");
			ButtonWidget done = new ButtonWidget.Builder(doneText, (widget) -> {
				ModDataFiles.MAIN_CONFIG_FILE.setValueAndSave(CONFIG);
				close();
			}).dimensions(distFromLeft + closeWithoutSaving.getWidth() + 10, this.height - 30, closeWithoutSaving.getWidth(), 20).build();
			
			addDrawableChild(done);
			
			

		}
		
		addToggle("showItemEmcOrigin");
		addToggle("showEnchantedBookRepairCost");
		addToggle("mapper.enabled");
		addToggle("transmutationTable.animated");
		addToggle("transmutationTable.floorButton");
	}

	@Override
	public void removed() {
		
	}
	
	private void addToggle(String key) {
		
		String[] comments = COMMENTS.get(key);
		MutableText commentsText = Text.empty();
		for (String comment : comments) {
			commentsText.append("// ");
			commentsText.append(Text.literal(comment));
			commentsText.append("\n");
		}
		
		Text f = Text.of("False");
		Text t = Text.of("True");
		int width = textRenderer.getWidth(key) + 15;
		TextWidget value = new TextWidget(distFromLeft + width + 20, lastY + 10, 0, 0, (boolean)CONFIG.get(key) ? t : f, textRenderer);
		addDrawable(value);
		addDrawableChild(new ButtonWidget.Builder(Text.of(key), (widget) -> {
			boolean newValue = !(boolean)CONFIG.get(key);
			CONFIG.put(key, newValue);
			value.setMessage(newValue ? t : f);
		}).dimensions(distFromLeft, lastY, width, 20).tooltip(Tooltip.of(commentsText)).build());
		
		lastY += 25;
	}
	
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
		GameOptionsScreen.drawCenteredTextWithShadow(matrices, this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
	}
}
