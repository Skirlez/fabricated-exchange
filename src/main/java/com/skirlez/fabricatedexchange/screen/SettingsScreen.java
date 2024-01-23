package com.skirlez.fabricatedexchange.screen;

import java.util.Map;

import org.joml.Quaternionf;

import com.skirlez.fabricatedexchange.item.ModItems;
import com.skirlez.fabricatedexchange.util.SuperNumber;
import com.skirlez.fabricatedexchange.util.config.ModDataFiles;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class SettingsScreen extends GameOptionsScreen {
	private Map<String, Object> CONFIG;
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
			
			Text resetToDefaultText = Text.of("Reset To Default");
			ButtonWidget resetToDefault = new ButtonWidget.Builder(resetToDefaultText, (widget) -> {
				CONFIG = ModDataFiles.MAIN_CONFIG_FILE.copyDefaultValue();
				clearAndInit();
			}).dimensions(distFromLeft, this.height - 30, textRenderer.getWidth(resetToDefaultText) + 20, 20).build();
			
			addDrawableChild(resetToDefault);
			
			Text closeWithoutSavingText = Text.of("Close Without Saving");
			ButtonWidget closeWithoutSaving = new ButtonWidget.Builder(closeWithoutSavingText, (widget) -> {
				close();
			}).dimensions(distFromLeft + resetToDefault.getWidth() + 10, this.height - 30, textRenderer.getWidth(closeWithoutSavingText) + 20, 20).build();
			
			addDrawableChild(closeWithoutSaving);
			
			Text doneText = Text.of("Done");
			ButtonWidget done = new ButtonWidget.Builder(doneText, (widget) -> {
				ModDataFiles.MAIN_CONFIG_FILE.setValueAndSave(CONFIG);
				close();
			}).dimensions(distFromLeft + resetToDefault.getWidth() + closeWithoutSaving.getWidth() + 20, this.height - 30, closeWithoutSaving.getWidth(), 20).build();
			
			addDrawableChild(done);
			
			

		}
		
		addBooleanValue("showItemEmcOrigin");
		addBooleanValue("showEnchantedBookRepairCost");
		addSuperNumberValue("enchantmentEmcConstant");
		addSuperNumberValue("emcInMultiplier");
		addSuperNumberValue("emcOutMultiplier");
		addBooleanValue("mapper.enabled");
		addBooleanValue("transmutationTable.animated");
		addBooleanValue("transmutationTable.floorButton");
		
	}

	@Override
	public void removed() {
		
	}
	
	private void addSuperNumberValue(String key) {
		String[] comments = COMMENTS.get(key);
		MutableText commentsText = Text.empty();
		for (String comment : comments) {
			commentsText.append("// ");
			commentsText.append(Text.literal(comment));
			commentsText.append("\n");
		}
		

		
		
		Text nameText = Text.of(key + ":");
		int width = textRenderer.getWidth(nameText);
		int height = 12;
		TextWidget nameWidget = new TextWidget(distFromLeft, lastY + height / 2, width, height, nameText, textRenderer);
		nameWidget.setTooltip(Tooltip.of(commentsText));
		addDrawableChild(nameWidget);
		
	

		
		TextFieldWidget field = new TextFieldWidget(this.textRenderer, distFromLeft + width + 10, lastY + height / 2 - 1, 50, height, Text.empty());

		Text invalidText = Text.of("Invalid number!!");
		int invalidWidth = textRenderer.getWidth(invalidText);
		TextWidget invalidTextWidget = new TextWidget(distFromLeft + nameWidget.getWidth() + field.getWidth() + 20, lastY + height / 2, invalidWidth, height, invalidText, textRenderer);
		addDrawable(invalidTextWidget);
		
		
		field.setChangedListener((str) -> {
			boolean isNumberValid = SuperNumber.isValidNumberString(str);
			invalidTextWidget.visible = !isNumberValid;
			if (isNumberValid)
				CONFIG.put(key, str);
		});
		field.setText((String)CONFIG.get(key));
		addDrawableChild(field);
		
		lastY += 25;
	}
	
	
	private void addBooleanValue(String key) {
		String[] comments = COMMENTS.get(key);
		MutableText commentsText = Text.empty();
		for (String comment : comments) {
			commentsText.append("// ");
			commentsText.append(Text.literal(comment));
			commentsText.append("\n");
		}
		
		Text f = Text.of(key + ": OFF");
		Text t = Text.of(key + ": ON");
		int width = textRenderer.getWidth(f) + 15;
		//TextWidget value = new TextWidget(distFromLeft + width + 20, lastY + 10, 0, 0, (boolean)CONFIG.get(key) ? t : f, textRenderer);
		//addDrawable(value);
		addDrawableChild(new ButtonWidget.Builder((boolean)CONFIG.get(key) ? t : f, (widget) -> {
			boolean newValue = !(boolean)CONFIG.get(key);
			CONFIG.put(key, newValue);
			widget.setMessage(newValue ? t : f);
		}).dimensions(distFromLeft, lastY, width, 20).tooltip(Tooltip.of(commentsText)).build());
		
		lastY += 25;
	}
	
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
		GameOptionsScreen.drawCenteredTextWithShadow(matrices, this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
		itemRenderer.renderGuiItemIcon(matrices, new ItemStack(ModItems.DARK_MATTER_HOE), mouseX - 8, mouseY - 8);
	}
}
