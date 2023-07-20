package com.skirlez.fabricatedexchange.screen;


import com.mojang.blaze3d.systems.RenderSystem;
import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.emc.EmcData;
import com.skirlez.fabricatedexchange.mixin.client.HandledScreenAccessor;
import com.skirlez.fabricatedexchange.screen.slot.FakeSlot;
import com.skirlez.fabricatedexchange.screen.slot.FuelSlot;
import com.skirlez.fabricatedexchange.screen.slot.collection.OutputSlot;
import com.skirlez.fabricatedexchange.util.SuperNumber;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class EnergyCollectorScreen extends HandledScreen<FuelScreenHandler> {
    private final Identifier texture;
    private double fuelProgress; // 0 - 1 how much of the arrow should be displayed
    private double emcProgress; // 0 - 1 how much of the emc bar should be filled
    private final EnergyCollectorScreenHandler handler;
    private int light;
    private SuperNumber emc;
    private final int level;
    private final SuperNumber maximumEmc;
    public EnergyCollectorScreen(FuelScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);        
        this.level = handler.getLevel();
        if (this.level == 0)
            maximumEmc = new SuperNumber(10000);
        else if (this.level == 1)
            maximumEmc = new SuperNumber(30000);
        else 
            maximumEmc = new SuperNumber(60000);

        this.handler = (EnergyCollectorScreenHandler)handler;
        PacketByteBuf buf = handler.getAndConsumeCreationBuffer();
        if (buf != null) {
            emc = new SuperNumber(buf.readString());
            light = buf.readInt();
            calculateFuelProgress();
            SuperNumber emcCopy = new SuperNumber(emc);
            emcCopy.divide(maximumEmc);
            emcProgress = emcCopy.toDouble();
        }  
        else {
            emc = SuperNumber.Zero();
            light = 0;
            fuelProgress = 0d;
            emcProgress = 0d;
        }

        texture = new Identifier(FabricatedExchange.MOD_ID, "textures/gui/collector" + String.valueOf(level + 1) + ".png");
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
        SuperNumber emcCopy = new SuperNumber(emc);
        emcCopy.divide(maximumEmc);
        emcProgress = emcCopy.toDouble();

        calculateFuelProgress();
    }

    @Override
    protected void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType) {
        if (actionType == SlotActionType.QUICK_MOVE && slot instanceof FuelSlot) {
            ((HandledScreenAccessor)this).setDoubleClicking(false);
        }
        
        super.onMouseClick(slot, slotId, button, actionType);
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
        SuperNumber emcCopy = new SuperNumber(emc);
        emcCopy.floor();
        textRenderer.draw(matrices, emcCopy.toString(), 62 + xOffset, 32, 0x404040);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        drawMouseoverTooltip(matrices, mouseX, mouseY);
    }


    public void update(SuperNumber emc, int light) {
        this.emc = emc;
        this.light = light;
    }

    public void calculateFuelProgress() {
        fuelProgress = 0d;
        FuelSlot fuelSlot = (FuelSlot)handler.getSlot(0);
        FakeSlot targetSlot = (FakeSlot)handler.getSlot(2);
        ItemStack stack = fuelSlot.getStack();
        if (stack.isEmpty())
            return;
        Item item = stack.getItem();
        if (!FabricatedExchange.fuelProgressionMap.containsKey(item))
            return;
        SuperNumber itemEmc = EmcData.getItemEmc(item);
        if (targetSlot.hasStack()) {
            SuperNumber targetItemEmc = EmcData.getItemEmc(targetSlot.getStack().getItem());
            if (itemEmc.compareTo(targetItemEmc) >= 0)
                return;
        }

        Item nextItem = FabricatedExchange.fuelProgressionMap.get(item);
        OutputSlot outputSlot = (OutputSlot)handler.getSlot(1);
        if ((!nextItem.equals(outputSlot.getStack().getItem())
                || outputSlot.getStack().getMaxCount() <= outputSlot.getStack().getCount()
                ) && outputSlot.hasStack())
            return; // return if there's an item in the output slot that we cannot merge with the next item in the fuel progression
        SuperNumber nextEmc = EmcData.getItemEmc(nextItem);
        
        nextEmc.subtract(itemEmc);

        SuperNumber emcCopy = new SuperNumber(emc);
        emcCopy.divide(nextEmc);
        fuelProgress = emcCopy.toDouble();
        if (fuelProgress > 1d)
            fuelProgress = 1d;
    }
}
