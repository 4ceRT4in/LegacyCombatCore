package dev.legacycombatcore.mixin;

import dev.legacycombatcore.combat.LegacyDamage;
import dev.legacycombatcore.combat.LegacyKnockback;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
abstract class LivingEntityMixin {
	@Shadow
	protected abstract void playHurtSound(DamageSource source);

	@Redirect(
			method = "getDamageAfterArmorAbsorb",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/damagesource/CombatRules;getDamageAfterAbsorb(Lnet/minecraft/world/entity/LivingEntity;FLnet/minecraft/world/damagesource/DamageSource;FF)F"
			)
	)
	private float legacyCombatCore$applyLegacyArmorReduction(
			LivingEntity entity,
			float damage,
			DamageSource source,
			float armor,
			float toughness
	) {
		return LegacyDamage.applyArmorReduction(damage, armor);
	}

	@Redirect(
			method = "getDamageAfterMagicAbsorb",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/damagesource/CombatRules;getDamageAfterMagicAbsorb(FF)F"
			)
	)
	private float legacyCombatCore$applyLegacyEnchantmentProtection(
			float damage,
			float protection,
			DamageSource source,
			float originalDamage
	) {
		return LegacyDamage.applyEnchantmentProtection((LivingEntity)(Object)this, source, damage);
	}

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

	@Redirect(
			method = "hurt",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/LivingEntity;playHurtSound(Lnet/minecraft/world/damagesource/DamageSource;)V"
			)
	)
	private void legacyCombatCore$useShieldBlockSoundForBlockedHits(
			LivingEntity target,
			DamageSource source
	) {
		if (target.isDamageSourceBlocked(source)) {
			if (target instanceof Player defendingPlayer) {
				// The entity event plays the shield sound for the defending local player.
				// Send it separately to nearby players because remote player entities do
				// not replay that event sound on the attacker's client.
				target.level().playSound(
						defendingPlayer,
						target.getX(),
						target.getY(),
						target.getZ(),
						SoundEvents.SHIELD_BLOCK,
						SoundSource.PLAYERS,
						1.0F,
						0.8F + target.level().random.nextFloat() * 0.4F
				);
			}
		} else {
			this.playHurtSound(source);
		}
	}
}
