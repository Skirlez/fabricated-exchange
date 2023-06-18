package com.skirlez.fabricatedexchange.screen;

import java.lang.reflect.Field;

import com.mojang.blaze3d.systems.RenderSystem;
import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.screen.slot.collection.FuelSlot;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class EnergyCollectorScreen extends HandledScreen<EnergyCollectorScreenHandler> {
    private static final Identifier TEXTURE =
            new Identifier(FabricatedExchange.MOD_ID, "textures/gui/collector1.png");

    public Field fieldDoubleClicked;
    public EnergyCollectorScreen(EnergyCollectorScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        try {
            fieldDoubleClicked = this.getClass().getSuperclass().getDeclaredField("doubleClicking");
            fieldDoubleClicked.setAccessible(true);
        } catch (NoSuchFieldException | SecurityException e) {
            FabricatedExchange.LOGGER.error("", e);
        }
    }

    @Override
    protected void init() {
        super.init();
        titleX = 0; 
        titleY = 0; 
        this.backgroundWidth = 176;
        this.backgroundHeight = 166;
    }

    @Override
    protected void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType) {
        if (actionType == SlotActionType.QUICK_MOVE && slot instanceof FuelSlot) {
            try {
                fieldDoubleClicked.set(this, Boolean.FALSE);
            } catch (SecurityException | IllegalAccessException e) {
                FabricatedExchange.LOGGER.error("", e);
            }
        }
        super.onMouseClick(slot, slotId, button, actionType);
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight);
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
