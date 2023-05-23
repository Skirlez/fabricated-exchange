package com.skirlez.fabricatedexchange.screen;


import java.lang.reflect.Field;

import com.mojang.blaze3d.systems.RenderSystem;
import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.FabricatedExchangeClient;
import com.skirlez.fabricatedexchange.screen.slot.TransmutationSlot;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class TransmutationTableScreen extends HandledScreen<TransmutationTableScreenHandler> {
    
    
    private static final Identifier TEXTURE =
            new Identifier(FabricatedExchange.MOD_ID, "textures/gui/transmute.png");

    public Field fieldDoubleClicked;

    public TransmutationTableScreen(TransmutationTableScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        try {
            fieldDoubleClicked = this.getClass().getSuperclass().getDeclaredField("doubleClicking");
            fieldDoubleClicked.setAccessible(true);
        } catch (NoSuchFieldException | SecurityException e) {
            FabricatedExchange.LOGGER.error("", e);
        }

    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        // todo make this only for when clicking on the transmutation slots

        return super.mouseReleased(mouseX, mouseY, button);
    }
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean result = super.mouseClicked(mouseX, mouseY, button);
        Slot slot = getSlotAt(mouseX, mouseY);

        // this double clicking variable creates a lot of trouble for transmutation slots, so we attempt to make it false here
        if (slot instanceof TransmutationSlot) {
            try {
                fieldDoubleClicked.set(this, Boolean.FALSE);
            } catch (SecurityException | IllegalAccessException e) {
                FabricatedExchange.LOGGER.error("", e);
            }
        }
        return result;
    }

    private Slot getSlotAt(double x, double y) {
        for (int i = 0; i < ((ScreenHandler)this.handler).slots.size(); ++i) {
            Slot slot = ((ScreenHandler)this.handler).slots.get(i);
            if (!isPointWithinBounds(slot.x, slot.y, 16, 16, x, y) || !slot.isEnabled()) continue;
                return slot;
        }
        return null;
    }

    @Override
    protected void init() {
        super.init();
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2 - 55; 
        titleY = -24; 
        this.backgroundWidth = 228;
        this.backgroundHeight = 196;
        
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - backgroundWidth) / 2 - 1;
        int y = (height - backgroundHeight) / 2 - 16;
        drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight);
    }

    @Override
    protected boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button) {
        return false; // TODO: check if you're outside of the window bounds
    }

    @Override
    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
        this.textRenderer.draw(matrices, this.title, (float)this.titleX, (float)this.titleY, 0x404040);
        String emc = FabricatedExchangeClient.clientEmc.toString();
        this.textRenderer.draw(matrices, Text.empty().append("EMC: "), -20, 70, 0x404040);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        drawMouseoverTooltip(matrices, mouseX, mouseY);
    }
}
