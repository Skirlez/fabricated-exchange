package com.skirlez.fabricatedexchange.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.mixin.client.HandledScreenAccessor;
import com.skirlez.fabricatedexchange.screen.slot.FuelSlot;
import com.skirlez.fabricatedexchange.util.SuperNumber;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class AntiMatterRelayScreen extends HandledScreen<AntiMatterRelayScreenHandler> {
    private final Identifier texture;
    private final int level;
    private final SuperNumber emc;
    private final SuperNumber maximumEmc;
    private final AntiMatterRelayScreenHandler handler;
    public AntiMatterRelayScreen(AntiMatterRelayScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.handler = handler;
        this.level = handler.getLevel();
        PacketByteBuf buf = handler.getAndConsumeCreationBuffer();
        if (buf == null) 
            this.emc = SuperNumber.Zero();
        else
            this.emc = new SuperNumber(buf.readString());

        if (level == 0) 
            maximumEmc = new SuperNumber(100000);
        else if (level == 1) 
            maximumEmc = new SuperNumber(1000000);
        else 
            maximumEmc = new SuperNumber(10000000);
        
        texture = new Identifier(FabricatedExchange.MOD_ID, 
            "textures/gui/antimatter_relay" + String.valueOf(level + 1) + ".png");


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
        ItemStack fuelStack = handler.getFuelSlot().getStack();
        percent = (double)fuelStack.getCount() / fuelStack.getMaxCount();
        drawTexture(matrices, x + 64 + xFuel, y + 67 + yFuel, 0, backgroundHeight, (int)(30 * percent), 10); 

        // draw emc bar
        SuperNumber emcCopy = new SuperNumber(emc);
        emcCopy.divide(maximumEmc);
        percent = emcCopy.toDouble();
        if (percent > 1)
            percent = 1;
        drawTexture(matrices, x + 64 + xBar, y + 6, 30, backgroundHeight, (int)(102 * percent), 10); 

    }
    
    @Override
    protected void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType) {
        if (actionType == SlotActionType.QUICK_MOVE && slot instanceof FuelSlot)
            ((HandledScreenAccessor)this).setDoubleClicking(false);
        super.onMouseClick(slot, slotId, button, actionType);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    @Override
    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
        SuperNumber emcCopy = new SuperNumber(emc);
        emcCopy.floor();
        int xOffset;
        if (level == 0) 
            xOffset = 0;
        else if (level == 1) 
            xOffset = 17;
        else 
            xOffset = 37;
        

        textRenderer.draw(matrices, emcCopy.toString(), 88 + xOffset, 16, 0x404040);
    }

    public void update(SuperNumber emc) {
        this.emc.copyValueOf(emc);
    }
}
