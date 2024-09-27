package com.skirlez.fabricatedexchange.sound;

import com.skirlez.fabricatedexchange.FabricatedExchange;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ModSoundEvents {

	public static SoundEvent PS_USE = registerSoundEvent("philosophers_stone_use");
	public static SoundEvent ITEM_CHARGE = registerSoundEvent("item_charge");
	public static SoundEvent ITEM_DISCHARGE = registerSoundEvent("item_discharge");
	public static SoundEvent WIND_PROJECTILE_FIRE = registerSoundEvent("wind_projectile_fire");

	private static SoundEvent registerSoundEvent(String name) {
		Identifier id = new Identifier(FabricatedExchange.MOD_ID, name);
		return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
	}

	public static void register() {
		// (:
	}
	
}
