package dev.legacycombatcore.client.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
abstract class EntityPickRadiusMixin {
	@Inject(method = "getPickRadius", at = @At("HEAD"), cancellable = true)
	private void legacyCombatCore$restoreLegacyLivingEntityPickBorder(
			CallbackInfoReturnable<Float> callbackInfo
	) {
		if ((Object)this instanceof LivingEntity) {
			callbackInfo.setReturnValue(0.1F);
		}
	}
}
