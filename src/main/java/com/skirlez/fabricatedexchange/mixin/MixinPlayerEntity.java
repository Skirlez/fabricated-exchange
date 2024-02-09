package com.skirlez.fabricatedexchange.mixin;

import com.skirlez.fabricatedexchange.item.rings.SwiftWolfsRendingGale;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity {

	@Inject(method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;", at = @At("HEAD"))
	private void onDropItem(ItemStack stack, boolean throwRandomly, boolean retainOwnership, CallbackInfoReturnable<ItemEntity> cir) {
		if (!stack.isEmpty() && stack.getItem() instanceof SwiftWolfsRendingGale) {
			PlayerEntity player = (PlayerEntity) (Object) this;
			if (!player.world.isClient) {
				player.getAbilities().allowFlying = false;
				player.getAbilities().flying = false;
				player.sendAbilitiesUpdate();
			}
		}
	}
}