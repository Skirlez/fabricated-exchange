package com.skirlez.fabricatedexchange.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.block.AlchemicalChestBlockEntity;
import com.skirlez.fabricatedexchange.block.EnergyCondenserBlockEntity;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;

@Mixin(TexturedRenderLayers.class)
public abstract class ModTexturedRenderLayers {
	private static final SpriteIdentifier ALCHEMICAL_CHEST = 
		new SpriteIdentifier(TexturedRenderLayers.CHEST_ATLAS_TEXTURE, new Identifier(FabricatedExchange.MOD_ID, "entity/chest/alchemical_chest"));
	private static final SpriteIdentifier ENERGY_CONDENSER_MK1 = 
		new SpriteIdentifier(TexturedRenderLayers.CHEST_ATLAS_TEXTURE, new Identifier(FabricatedExchange.MOD_ID, "entity/chest/energy_condenser_mk1"));
	private static final SpriteIdentifier ENERGY_CONDENSER_MK2 = 
		new SpriteIdentifier(TexturedRenderLayers.CHEST_ATLAS_TEXTURE, new Identifier(FabricatedExchange.MOD_ID, "entity/chest/energy_condenser_mk2"));


	@Inject(method = "getChestTextureId", at = @At("HEAD"), cancellable = true)
	private static void addMoreTextureIds(BlockEntity blockEntity, ChestType type, boolean christmas, CallbackInfoReturnable<SpriteIdentifier> cir) {
		if (blockEntity instanceof AlchemicalChestBlockEntity)
			cir.setReturnValue(ALCHEMICAL_CHEST);
		else if (blockEntity instanceof EnergyCondenserBlockEntity condenser) {
			if (condenser.getLevel() == 0)
				cir.setReturnValue(ENERGY_CONDENSER_MK1);
			else
				cir.setReturnValue(ENERGY_CONDENSER_MK2);
		}
	}
}
