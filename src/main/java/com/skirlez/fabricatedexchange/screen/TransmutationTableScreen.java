package com.skirlez.fabricatedexchange.screen;

import java.util.function.Consumer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.FabricatedExchangeClient;
import com.skirlez.fabricatedexchange.mixin.HandledScreenAccessor;
import com.skirlez.fabricatedexchange.networking.ModMessages;
import com.skirlez.fabricatedexchange.screen.slot.transmutation.TransmutationSlot;

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

public class TransmutationTableScreen extends HandledScreen<TransmutationTableScreenHandler> {
    
    public TextFieldWidget searchBar;
    private static final Identifier TEXTURE =
            new Identifier(FabricatedExchange.MOD_ID, "textures/gui/transmute.png");

    private String oldSearchText = "";
    public TransmutationTableScreen(TransmutationTableScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
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

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        drawMouseoverTooltip(matrices, mouseX, mouseY);
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
        PacketByteBuf buffer = PacketByteBufs.create();
        buffer.writeInt(num);
        ClientPlayNetworking.send(ModMessages.TRANSMUTATION_TABLE_WIDGETS, buffer);
    }


    public Slot getFocusedSlot() {
        return focusedSlot;
    }
}
