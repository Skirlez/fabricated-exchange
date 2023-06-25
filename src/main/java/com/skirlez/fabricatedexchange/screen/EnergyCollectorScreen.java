package com.skirlez.fabricatedexchange.screen;

import java.lang.reflect.Field;
import com.mojang.blaze3d.systems.RenderSystem;
import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.emc.EmcData;
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
    private static final Identifier TEXTURE =
            new Identifier(FabricatedExchange.MOD_ID, "textures/gui/collector1.png");
    private double progress;
    private final EnergyCollectorScreenHandler handler;
    private int light;
    private SuperNumber emc;

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
        progress = 0d;
        this.handler = handler;
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
    protected void handledScreenTick() {
        emc.add(SuperNumber.ONE);
        progress = 0d;
        FuelSlot fuelSlot = (FuelSlot)handler.getSlot(0);
        ItemStack stack = fuelSlot.getStack();
        if (stack.isEmpty())
            return;
        Item item = stack.getItem();
        if (!FabricatedExchange.fuelProgressionMap.containsKey(item))
            return;
        SuperNumber itemEmc = EmcData.getItemEmc(item);
        Item nextItem = FabricatedExchange.fuelProgressionMap.get(item);
        SuperNumber nextEmc = EmcData.getItemEmc(nextItem);
        OutputSlot outputSlot = (OutputSlot)handler.getSlot(handler.getOutputSlotIndex());
        if ((!nextItem.equals(outputSlot.getStack().getItem())
                || outputSlot.getStack().getMaxCount() <= outputSlot.getStack().getCount()
                ) && outputSlot.hasStack())
            return; // return if there's an item in the output slot that we cannot merge with the next item in the progression
        nextEmc.subtract(itemEmc);
        SuperNumber emcCopy = new SuperNumber(emc);
        emcCopy.divide(nextEmc);
        progress = emcCopy.toDouble();
        if (progress > 1d)
            progress = 1d;
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

        final int maxProgress = 26;
        int offset = maxProgress - (int)(progress * maxProgress);
        drawTexture(matrices, x + 138, y + 30 + offset, 176, 13 + offset, 12, 24);

        int height = (int)(((float)light) / 15 * 12);
        drawTexture(matrices, x + 126, y + 49 - height, 177, 12 - height, 12, height);
    }

    @Override
    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
        textRenderer.draw(matrices, emc.toString(), 63, 32, 0x404040);
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
