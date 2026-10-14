package dev.legacycombatcore.mixin;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Projectile.class)
abstract class ProjectileMixin {
	@Unique
	private static final float LEGACY_SPAWN_OFFSET = 0.16F;
	@Unique
	private static final double LEGACY_INACCURACY = 0.0075;
	@Unique
	private static final double MODERN_TRIANGLE_DEVIATION = 0.0172275;
	@Unique
	private static final int LEGACY_OWNER_IMMUNITY_TICKS = 5;

	@Inject(method = "canHitEntity", at = @At("HEAD"), cancellable = true)
	private void legacyCombatCore$useTimedOwnerImmunity(
			Entity target,
			CallbackInfoReturnable<Boolean> callbackInfo
	) {
		Projectile projectile = (Projectile)(Object)this;
		Entity owner = projectile.getOwner();
		if (legacyCombatCore$usesLegacyTrajectory(projectile) && owner != null && target == owner) {
			boolean validTarget = !target.isSpectator() && target.isAlive() && target.isPickable();
			callbackInfo.setReturnValue(validTarget && projectile.tickCount >= LEGACY_OWNER_IMMUNITY_TICKS);
		}
	}

	@Inject(method = "shootFromRotation", at = @At("HEAD"))
	private void legacyCombatCore$applyLegacySpawnOffset(
			Entity shooter,
			float xRotation,
			float yRotation,
			float yRotationOffset,
			float velocity,
			float inaccuracy,
			CallbackInfo callbackInfo
	) {
		Projectile projectile = (Projectile)(Object)this;
		if (!legacyCombatCore$usesLegacyTrajectory(projectile)) {
			return;
		}

		float yawRadians = yRotation * (float)(Math.PI / 180.0);
		projectile.setPos(
				projectile.getX() - Mth.cos(yawRadians) * LEGACY_SPAWN_OFFSET,
				projectile.getY(),
				projectile.getZ() - Mth.sin(yawRadians) * LEGACY_SPAWN_OFFSET
		);
	}

	@Redirect(
			method = "shootFromRotation",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/Entity;getKnownMovement()Lnet/minecraft/world/phys/Vec3;"
			)
	)
	private Vec3 legacyCombatCore$doNotInheritShooterMovement(Entity shooter) {
		Projectile projectile = (Projectile)(Object)this;
		return legacyCombatCore$usesLegacyTrajectory(projectile) ? Vec3.ZERO : shooter.getKnownMovement();
	}

	@Redirect(
			method = "getMovementToShoot",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/util/RandomSource;triangle(DD)D"
			)
	)
	private double legacyCombatCore$useLegacyGaussianInaccuracy(RandomSource random, double mode, double deviation) {
		Projectile projectile = (Projectile)(Object)this;
		if (!legacyCombatCore$usesLegacyTrajectory(projectile)) {
			return random.triangle(mode, deviation);
		}

		double inaccuracy = deviation / MODERN_TRIANGLE_DEVIATION;
		return mode + random.nextGaussian() * LEGACY_INACCURACY * inaccuracy;
	}

	@Unique
	private static boolean legacyCombatCore$usesLegacyTrajectory(Projectile projectile) {
		return projectile instanceof ThrowableProjectile
				|| projectile instanceof AbstractArrow && !(projectile instanceof ThrownTrident);
	}
}
