package com.skirlez.fabricatedexchange.screen;


import com.mojang.blaze3d.systems.RenderSystem;
import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.util.IPlayerDataSaver;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class TransmutationTableScreen extends HandledScreen<TransmutationTableScreenHandler>  {
    private static final Identifier TEXTURE =
            new Identifier(FabricatedExchange.MOD_ID, "textures/gui/transmute.png");

    public TransmutationTableScreen(TransmutationTableScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
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
    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
        this.textRenderer.draw(matrices, this.title, (float)this.titleX, (float)this.titleY, 0x404040);
        NbtCompound nbt = (NbtCompound) ((IPlayerDataSaver) (this.client.player)).getPersistentData();
        String emc = nbt.getString("emc");
        FabricatedExchange.LOGGER.info(emc);
        this.textRenderer.draw(matrices, Text.empty().append("EMC: " + emc), -20, 70, 0x404040);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        drawMouseoverTooltip(matrices, mouseX, mouseY);
    }
    
}
