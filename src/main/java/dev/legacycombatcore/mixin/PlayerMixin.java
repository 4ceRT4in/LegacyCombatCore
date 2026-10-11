package dev.legacycombatcore.mixin;

import dev.legacycombatcore.combat.LegacyKnockback;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Player.class)
abstract class PlayerMixin {
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
}
