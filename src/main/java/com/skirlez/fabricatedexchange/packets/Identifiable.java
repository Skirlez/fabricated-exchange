package com.skirlez.fabricatedexchange.packets;

import com.skirlez.fabricatedexchange.FabricatedExchange;

import net.minecraft.util.Identifier;

class Identifiable {
	protected final Identifier id;
	public Identifiable(String name) {
		id = new Identifier(FabricatedExchange.MOD_ID, name);
	}
	public Identifier getId() {
		return id;
	}
}
