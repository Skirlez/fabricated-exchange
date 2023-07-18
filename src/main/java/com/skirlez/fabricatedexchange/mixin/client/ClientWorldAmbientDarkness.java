package com.skirlez.fabricatedexchange.mixin.client;
// tell the clientworld to calculate ambient darkness, since it doesn't for some reason?

import java.util.function.BooleanSupplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.World;

@Mixin(ClientWorld.class)
public class ClientWorldAmbientDarkness {

    @Inject(method = "tick", at = @At("HEAD"))
    public void addCalculationToTick(BooleanSupplier shouldKeepTicking, CallbackInfo info) {
        ((World)(Object)this).calculateAmbientDarkness();
    }
}
