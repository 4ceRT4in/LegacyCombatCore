package dev.legacycombatcore.mixin;

import dev.legacycombatcore.combat.LegacyKnockback;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
abstract class LivingEntityMixin {
	@Redirect(
			method = "hurt",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V"
			)
	)
	private void legacyCombatCore$applyLegacyBaseKnockback(
			LivingEntity target,
			double strength,
			double directionX,
			double directionZ,
			DamageSource source,
			float damage
	) {
		LegacyKnockback.applyBaseKnockback(target, source.getEntity());
	}
}
