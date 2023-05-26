package com.skirlez.fabricatedexchange.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.skirlez.fabricatedexchange.screen.slot.TransmutationSlot;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;

import org.spongepowered.asm.mixin.injection.At;

@Mixin(HandledScreen.class)
public abstract class ModHandledScreen {
    @Shadow
    protected boolean isPointWithinBounds(int x, int y, int width, int height, double pointX, double pointY) {
        // this code never gets executed, the function needs to have a body 
        // to get the compiler to shut up. what a sad fate for this line. to never be executed.
        return false;
    }

    // we do this so empty transmutation slots do not highlight
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;isPointOverSlot(Lnet/minecraft/screen/slot/Slot;DD)Z"))
	public boolean injectRenderMethod(HandledScreen<?> screen, Slot slot, double pointX, double pointY) {
        if (slot instanceof TransmutationSlot && !slot.hasStack())
            return false;
        return isPointWithinBounds(slot.x, slot.y, 16, 16, pointX, pointY);
	}
}
