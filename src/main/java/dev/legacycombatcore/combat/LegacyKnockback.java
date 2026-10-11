package dev.legacycombatcore.combat;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

/**
 * Applies the knockback math used by Minecraft 1.8.9.
 */
public final class LegacyKnockback {
	private static final double BASE_KNOCKBACK = 0.4;
	private static final double MAX_VERTICAL_KNOCKBACK = 0.4000000059604645;
	private static final double MIN_DIRECTION_LENGTH_SQUARED = 1.0E-4;

	private LegacyKnockback() {
	}

	/**
	 * Reproduces {@code EntityLivingBase#knockBack} from Minecraft 1.8.9.
	 */
	public static void applyBaseKnockback(LivingEntity target, Entity attacker) {
		if (attacker == null
				|| target.getRandom().nextDouble() < target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE)) {
			return;
		}

		double directionX = attacker.getX() - target.getX();
		double directionZ = attacker.getZ() - target.getZ();

		while (directionX * directionX + directionZ * directionZ < MIN_DIRECTION_LENGTH_SQUARED) {
			directionX = (Math.random() - Math.random()) * 0.01;
			directionZ = (Math.random() - Math.random()) * 0.01;
		}

		float directionLength = (float)Math.sqrt(directionX * directionX + directionZ * directionZ);
		Vec3 movement = target.getDeltaMovement();
		double movementY = movement.y / 2.0 + BASE_KNOCKBACK;

		target.hasImpulse = true;
		target.setDeltaMovement(
				movement.x / 2.0 - directionX / directionLength * BASE_KNOCKBACK,
				Math.min(movementY, MAX_VERTICAL_KNOCKBACK),
				movement.z / 2.0 - directionZ / directionLength * BASE_KNOCKBACK
		);
	}

	/**
	 * Reproduces the direct velocity addition used for sprint and Knockback-enchantment bonuses in 1.8.9.
	 */
	public static void applyAttackBonus(LivingEntity target, double strength, double directionX, double directionZ) {
		target.push(-directionX * strength, 0.1, -directionZ * strength);
	}
}
