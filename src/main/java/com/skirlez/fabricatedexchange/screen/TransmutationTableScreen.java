package com.skirlez.fabricatedexchange.screen;

import java.util.function.Consumer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.FabricatedExchangeClient;
import com.skirlez.fabricatedexchange.mixin.client.HandledScreenAccessor;
import com.skirlez.fabricatedexchange.networking.ModMessages;
import com.skirlez.fabricatedexchange.screen.slot.transmutation.TransmutationSlot;
import com.skirlez.fabricatedexchange.util.ConfigFile;
import com.skirlez.fabricatedexchange.util.ModConfig;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

public class TransmutationTableScreen extends HandledScreen<TransmutationTableScreenHandler> {
    
    public TextFieldWidget searchBar;
    private static final Identifier TEXTURE =
            new Identifier(FabricatedExchange.MOD_ID, "textures/gui/transmute.png");

    private String oldSearchText = "";

    private long distStartTime = System.currentTimeMillis();
    private long angleStartTime = System.currentTimeMillis();
    private int rotationDir = 1;

    private double distanceFromCenter;
    private boolean animated;
    public TransmutationTableScreen(TransmutationTableScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        animated = ModConfig.CONFIG_FILE.getOption(ConfigFile.Bool.TRANSMUTATION_TABLE_ANIMATED);
        if (animated) 
            distanceFromCenter = 0.0;
        else 
            distanceFromCenter = 1.0;
        
    }

    @Override
    protected void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType) {
        // SWAP is when you press number keys to put slots in the hotbar. it does not consult the slot,
        // which for our case, means it duplicates the item. so we tell it to leave
        if (actionType == SlotActionType.SWAP && slot instanceof TransmutationSlot)
            return;
        super.onMouseClick(slot, slotId, button, actionType);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean result = super.mouseClicked(mouseX, mouseY, button);
        Slot slot = getSlotAt(mouseX, mouseY);

        // this double clicking variable creates a lot of trouble for transmutation slots, so we make it false here
        if (slot instanceof TransmutationSlot) {
            ((HandledScreenAccessor)this).setDoubleClicking(false);
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
        this.backgroundWidth = 231;
        this.backgroundHeight = 197;
        super.init();

        titleX = 4;     
        titleY = -10; 

        Consumer<String> updater = searchText -> updateSearchText(searchText);

        searchBar = new TextFieldWidget(this.textRenderer, x + 64, y - 31, 100, 10, Text.empty());
        searchBar.setMaxLength(30);
        searchBar.setChangedListener(updater);
        
        addDrawableChild(searchBar);
        
        addDrawableChild(ButtonWidget.builder(
            Text.of("<"),
            button -> updatePage(1)
        ).dimensions(x + 126, y + 83, 13, 13)
        .build());

        addDrawableChild(ButtonWidget.builder(
            Text.of(">"),
            button -> updatePage(2)
        ).dimensions(x + 191, y + 83, 13, 13)
        .build());

    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - backgroundWidth) / 2 - 1;
        int y = (height - backgroundHeight) / 2 - 17;
        
        drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight);
       
        
        
    }

    @Override
    protected boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button) {
        return false; // TODO: check if you're outside of the window bounds
    }


    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (searchBar.isFocused() && !(keyCode == 256 && scanCode == 1)) { // keycode == 256 && scanCode == 1 is escape key
            if (this.client.options.inventoryKey.matchesKey(keyCode, scanCode)) 
                return true;

        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
        textRenderer.draw(matrices, this.title, (float)this.titleX, (float)this.titleY, 0x404040);
        String emc = FabricatedExchangeClient.clientEmc.shortString();

        textRenderer.draw(matrices, Text.literal("EMC:"), 4, 79, 0x404040);
        textRenderer.draw(matrices, Text.literal(emc), 4, 89, 0x404040);

    }

    public void resetAngleTime() {
        angleStartTime = System.currentTimeMillis();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        drawMouseoverTooltip(matrices, mouseX, mouseY);

        DefaultedList<TransmutationSlot> list = handler.getTransmutationSlots();
        double angleOffset = 0.0;

        if (animated) {
            double angleTime = (double)(System.currentTimeMillis() - angleStartTime);
            double x = angleTime / 800.0;


            if (angleTime < 800.0)
                angleOffset = 360.0 * (Math.pow(x - 1.0, 3.0) + 1.0);
            

            double distTime = (double)(System.currentTimeMillis() - distStartTime);
            if (distTime < 800.0) {
                x = distTime / 800.0;
                distanceFromCenter = Math.pow(x - 1.0, 3.0) + 1.0;
            }
        }

        
        // outer ring
        for (int i = 0; i < 12; i++) {
            TransmutationSlot slot = list.get(i);
            double radianAngle = Math.toRadians(slot.angle + (angleOffset * rotationDir));
            int yOffset = (int)(Math.sin(radianAngle) * distanceFromCenter * 41.0);
            int xOffset = (int)(Math.cos(radianAngle) * distanceFromCenter * 41.0);


            slot.setPosition(158 + xOffset, 32 + yOffset);
        }
        
        // inner ring
        for (int i = 12; i < 16; i++) {
            TransmutationSlot slot = list.get(i);
            double radianAngle = Math.toRadians(slot.angle - angleOffset * rotationDir);
            int yOffset = (int)(Math.sin(radianAngle) * distanceFromCenter * 19.0);
            int xOffset = (int)(Math.cos(radianAngle) * distanceFromCenter * 19.0);
            slot.setPosition(158 + xOffset, 32 + yOffset);
        }
        


    }

    private void updateSearchText(String searchText) {
        if (searchText.equals(oldSearchText))
            return;
        oldSearchText = searchText;
        PacketByteBuf buffer = PacketByteBufs.create();
        buffer.writeInt(0);
        buffer.writeString(searchText);
        
        ClientPlayNetworking.send(ModMessages.TRANSMUTATION_TABLE_WIDGETS, buffer);
    }
    private void updatePage(int num) {

        if (num == 1) 
            rotationDir = -1;
        else
            rotationDir = 1;
        PacketByteBuf buffer = PacketByteBufs.create();
        buffer.writeInt(num);

        if (animated) {
            long add = 0;
            double angleTime = (double)(System.currentTimeMillis() - angleStartTime);
        
            if (angleTime < 800 && angleTime > 0) {

                double x = angleTime / 800.0;
                double angleOffset = (Math.pow(x - 1.0, 3.0) + 1.0);

                add = (long)(208.0 - 208.0 * angleOffset );
            }
            angleStartTime = System.currentTimeMillis() + add;
        }
        ClientPlayNetworking.send(ModMessages.TRANSMUTATION_TABLE_WIDGETS, buffer);
    }

    public Slot getFocusedSlot() {
        return focusedSlot;
    }

}
