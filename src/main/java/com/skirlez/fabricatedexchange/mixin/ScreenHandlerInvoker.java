package com.skirlez.fabricatedexchange.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

@Mixin(ScreenHandler.class)
public interface ScreenHandlerInvoker {
	@Invoker("addSlot")
	public Slot invokeAddSlot(Slot slot);
}
