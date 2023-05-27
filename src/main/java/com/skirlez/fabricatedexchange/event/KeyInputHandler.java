package com.skirlez.fabricatedexchange.event;

import org.lwjgl.glfw.GLFW;

import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.networking.ModMessages;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.PacketByteBuf;

public class KeyInputHandler {
    public static final String KEY_CATEGORY = "key.category.fabricated-exchange";
    public static final String KEY_CHARGE_ITEM = "key.fabricated-exchange.charge";

    public static KeyBinding chargingKey;

    public static void registerKeyInputs() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (chargingKey.wasPressed() && chargingKey.isPressed()) {
                PacketByteBuf buffer = PacketByteBufs.create();
                buffer.writeBoolean(Screen.hasShiftDown());
                ClientPlayNetworking.send(ModMessages.ITEM_CHARGE_IDENTIFIER, buffer);
            }
        });
    }

    public static void register() {
        chargingKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            KEY_CHARGE_ITEM,
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            KEY_CATEGORY
        ));

        registerKeyInputs();
    }
}
