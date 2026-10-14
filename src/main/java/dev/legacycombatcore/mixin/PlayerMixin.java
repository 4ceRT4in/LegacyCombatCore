package dev.legacycombatcore.mixin;

import dev.legacycombatcore.combat.LegacyKnockback;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
abstract class PlayerMixin {
	@Inject(method = "getAttackStrengthScale", at = @At("HEAD"), cancellable = true)
	private void legacyCombatCore$removeAttackCooldown(
			float partialTick,
			CallbackInfoReturnable<Float> callbackInfo
	) {
		// 1.8.9 attacks always used full base and enchantment damage and passed the
		// full-strength predicates used by critical and sprint-knockback attacks.
		callbackInfo.setReturnValue(1.0F);
	}

	@Redirect(
			method = "tick",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/player/Player;resetAttackStrengthTicker()V"
			)
	)
	private void legacyCombatCore$keepAttackStrengthWhenChangingItems(Player player) {
		// 1.8.9 had no item-change attack cooldown.
	}

	@Redirect(
			method = "attack",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/player/Player;resetAttackStrengthTicker()V"
			)
	)
	private void legacyCombatCore$doNotResetAttackStrengthAfterAttacking(Player player) {
		// There was no attack-strength timer to reset in 1.8.9.
	}

	@Redirect(
			method = "attack",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/player/Player;isSprinting()Z",
					ordinal = 1
			)
	)
	private boolean legacyCombatCore$allowCriticalHitsWhileSprinting(Player player) {
		// This is the sprint exclusion in the critical-hit predicate. The first call controls sprint knockback.
		return false;
	}

	@Redirect(
			method = "attack",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V"
			)
	)
	private void legacyCombatCore$applyLegacyAttackBonus(
			LivingEntity target,
			double strength,
			double directionX,
			double directionZ
	) {
		LegacyKnockback.applyAttackBonus(target, strength, directionX, directionZ);
	}

	@Redirect(
			method = "attack",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"
			)
	)
	private void legacyCombatCore$suppressModernAttackSounds(
			Level level,
			Player excludedPlayer,
			double x,
			double y,
			double z,
			SoundEvent sound,
			SoundSource source,
			float volume,
			float pitch
	) {
		// Minecraft 1.8.9 had no separate weak, strong, critical, knockback, sweep or no-damage attack sounds.
	}

	@Redirect(
			method = "attack",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/player/Player;sweepAttack()V"
			)
	)
	private void legacyCombatCore$suppressSweepAttackTexture(Player player) {
		// Keep the requested combat behavior, but do not spawn the modern white sweep particle.
	}
}
