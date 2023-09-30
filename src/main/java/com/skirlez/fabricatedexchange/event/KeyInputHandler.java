package com.skirlez.fabricatedexchange.event;

import com.skirlez.fabricatedexchange.item.ItemWithModes;
import org.lwjgl.glfw.GLFW;

import com.skirlez.fabricatedexchange.item.ChargeableItem;
import com.skirlez.fabricatedexchange.item.ExtraFunctionItem;
import com.skirlez.fabricatedexchange.networking.ModMessages;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;

public class KeyInputHandler {
	public static final String KEY_CATEGORY = "key.category.fabricated-exchange";
	public static final String KEY_CHARGE_ITEM = "key.fabricated-exchange.charge";
	public static final String KEY_MODE_ITEM = "key.fabricated-exchange.mode";
	public static final String KEY_EXTRA_FUNCTION = "key.fabricated-exchange.extra";


	public static KeyBinding chargingKey;
	public static KeyBinding modeChangeKey;
	public static KeyBinding extraFunctionKey;



	public static void registerKeyInputs() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (chargingKey.wasPressed()) {
				ItemStack stack = client.player.getStackInHand(Hand.MAIN_HAND);
				if (!(stack.getItem() instanceof ChargeableItem)) {
					stack = client.player.getStackInHand(Hand.OFF_HAND);
					if (!(stack.getItem() instanceof ChargeableItem))
						return;
				}

				PacketByteBuf buffer = PacketByteBufs.create();
				buffer.writeBoolean(Screen.hasShiftDown());
				ClientPlayNetworking.send(ModMessages.ITEM_CHARGE_IDENTIFIER, buffer);
	
				ChargeableItem item = (ChargeableItem)stack.getItem();
				int value = (Screen.hasShiftDown()) ? -1 : 1;
				ChargeableItem.chargeStack(stack, value, 0, item.getMaxCharge(), client.player);   
			}
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (extraFunctionKey.isPressed()) {
				ItemStack stack = client.player.getStackInHand(Hand.MAIN_HAND);
				if (!(stack.getItem() instanceof ExtraFunctionItem)) {
					return;
				}
				ClientPlayNetworking.send(ModMessages.EXTRA_FUNCTION_IDENTIFIER, PacketByteBufs.create());
				((ExtraFunctionItem)stack.getItem()).doExtraFunctionClient(stack, client.player);
			}
		});


		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (modeChangeKey.wasPressed()) {
				ItemStack stack = client.player.getStackInHand(Hand.MAIN_HAND);
				if (!(stack.getItem() instanceof ItemWithModes item))
					return;
			
				if (item.modeSwitchCondition(stack)) {
					ClientPlayNetworking.send(ModMessages.CYCLE_ITEM_MODE_IDENTIFIER, PacketByteBufs.create());
					ItemWithModes.cycleModes(stack, null);
					client.player.playSound(SoundEvents.BLOCK_STONE_BREAK, SoundCategory.PLAYERS, 1f, 1.4f);
				}
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

		modeChangeKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				KEY_MODE_ITEM,
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_G,
				KEY_CATEGORY
		));

		extraFunctionKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			KEY_EXTRA_FUNCTION,
			InputUtil.Type.KEYSYM,
			GLFW.GLFW_KEY_Z,
			KEY_CATEGORY
		));

		registerKeyInputs();
	}
}
