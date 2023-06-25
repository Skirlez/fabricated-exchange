package com.skirlez.fabricatedexchange.screen;

import java.lang.reflect.Field;
import com.mojang.blaze3d.systems.RenderSystem;
import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.emc.EmcData;
import com.skirlez.fabricatedexchange.screen.slot.FakeSlot;
import com.skirlez.fabricatedexchange.screen.slot.collection.FuelSlot;
import com.skirlez.fabricatedexchange.screen.slot.collection.OutputSlot;
import com.skirlez.fabricatedexchange.util.SuperNumber;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class EnergyCollectorScreen extends HandledScreen<EnergyCollectorScreenHandler> {
    private final Identifier texture;
    private double fuelProgress; // 0 - 1 how much of the arrow should be displayed
    private double emcProgress; // 0 - 1 how much of the emc bar should be filled
    private final EnergyCollectorScreenHandler handler;
    private int light;
    private SuperNumber emc;
    private final int level;
    private final SuperNumber maximumEmc;
    private final SuperNumber emcMultiplier;
    public Field fieldDoubleClicked;
    public EnergyCollectorScreen(EnergyCollectorScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        try {
            fieldDoubleClicked = this.getClass().getSuperclass().getDeclaredField("doubleClicking");
            fieldDoubleClicked.setAccessible(true);
        } catch (NoSuchFieldException | SecurityException e) {
            FabricatedExchange.LOGGER.error("", e);
        }
        emc = SuperNumber.Zero();
        fuelProgress = 0d;
        this.handler = handler;
        this.level = handler.getLevel();
        if (this.level == 0) {
            maximumEmc = new SuperNumber(10000);
            emcMultiplier = new SuperNumber(1, 5);
        }
        else if (this.level == 1) {
            maximumEmc = new SuperNumber(30000);
            emcMultiplier = new SuperNumber(3, 5);
        }
        else {
            maximumEmc = new SuperNumber(60000);
            emcMultiplier = new SuperNumber(2);
        }
        texture = new Identifier(FabricatedExchange.MOD_ID, "textures/gui/collector" + String.valueOf(level + 1) + ".png");
    }

    @Override
    protected void init() {
        super.init();
        titleX = 0; 
        titleY = 0; 
        
        int xOffset = 0;
        if (level == 1)
            xOffset = 24;
        else if (level == 2)
            xOffset = 43;
        this.backgroundWidth = 176 + xOffset;
        this.backgroundHeight = 166;
    }

    @Override
    protected void handledScreenTick() {
        SuperNumber addition = new SuperNumber(light, 15);
        addition.multiply(emcMultiplier);
        emc.add(addition);
        if (emc.compareTo(maximumEmc) == 1)
            emc.copyValueOf(maximumEmc);

        SuperNumber emcCopy = new SuperNumber(emc);
        emcCopy.divide(maximumEmc);
        emcProgress = emcCopy.toDouble();

        fuelProgress = 0d;
        FuelSlot fuelSlot = (FuelSlot)handler.getSlot(0);
        FakeSlot targetSlot = (FakeSlot)handler.getSlot(handler.getOutputSlotIndex() + 1);
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
        OutputSlot outputSlot = (OutputSlot)handler.getSlot(handler.getOutputSlotIndex());
        if ((!nextItem.equals(outputSlot.getStack().getItem())
                || outputSlot.getStack().getMaxCount() <= outputSlot.getStack().getCount()
                ) && outputSlot.hasStack())
            return; // return if there's an item in the output slot that we cannot merge with the next item in the fuelProgression
        SuperNumber nextEmc = EmcData.getItemEmc(nextItem);
        
        nextEmc.subtract(itemEmc);

        emcCopy.copyValueOf(emc);
        emcCopy.divide(nextEmc);
        fuelProgress = emcCopy.toDouble();
        if (fuelProgress > 1d)
            fuelProgress = 1d;
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
            xOffset = 3;
        else if (this.level == 2)
            xOffset = 11;
        SuperNumber emcCopy = new SuperNumber(emc);
        emcCopy.floor();
        textRenderer.draw(matrices, emcCopy.toString(), 63 + xOffset, 32, 0x404040);
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
}
