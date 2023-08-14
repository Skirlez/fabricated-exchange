package com.skirlez.fabricatedexchange.item;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public interface ItemWithModes {
    int getModeAmount();


    public static final String MODE_KEY = "FE_Mode";

    public static void cycleModes(ItemStack stack, @Nullable ServerPlayerEntity player) {
        if (!(stack.getItem() instanceof ItemWithModes item))
            return;
        int newMode = (getMode(stack) + 1) % item.getModeAmount();
        stack.getOrCreateNbt().putInt(MODE_KEY, newMode);

        if (player != null) {
            OverlayMessageS2CPacket packet = new OverlayMessageS2CPacket(
                    Text.translatable("item.fabricated-exchange.mode_switch")
                    .append(" ")
                    .append(Text.translatable(stack.getTranslationKey() + ".mode_" + (newMode + 1))));
            ((ServerPlayerEntity)player).networkHandler.sendPacket(packet);
        }
    }

    public static int getMode(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null)
            return 0;
        return nbt.getInt(MODE_KEY);
    }


}
