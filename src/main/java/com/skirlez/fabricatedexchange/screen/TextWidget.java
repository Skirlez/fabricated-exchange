package com.skirlez.fabricatedexchange.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class TextWidget
extends ButtonWidget {
    private final static PressAction NO_ACTION = new ButtonWidget.PressAction() {
		public void onPress(ButtonWidget widget) {	
			
		}
	};
    private final TextRenderer textRenderer;
    public TextWidget(int x, int y, int width, int height, Text text, TextRenderer textRenderer) {
        super(x, y, width, height, text, NO_ACTION); 
        this.textRenderer = textRenderer;
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        TextWidget.drawTextWithShadow(matrices, this.textRenderer, getMessage(), this.x, this.y, 0xFFFFFF | MathHelper.ceil(this.alpha * 255.0f) << 24);
    }
}
