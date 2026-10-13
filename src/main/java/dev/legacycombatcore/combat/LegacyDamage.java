package dev.legacycombatcore.combat;

import java.util.Random;
import net.minecraft.util.Mth;

/**
 * Damage-reduction formulas used by Minecraft 1.8.9.
 */
public final class LegacyDamage {
	private static final float DAMAGE_REDUCTION_DIVISOR = 25.0F;
	private static final int MAX_ARMOR = 20;
	private static final int MAX_RAW_ENCHANTMENT_PROTECTION = 25;
	private static final int MAX_EFFECTIVE_ENCHANTMENT_PROTECTION = 20;
	private static final Random PROTECTION_RANDOM = new Random();

	private LegacyDamage() {
	}

	/**
	 * Applies the pre-toughness linear armor formula: four percent reduction per armor point.
	 */
	public static float applyArmorReduction(float damage, float armor) {
		float effectiveArmor = Mth.clamp(armor, 0.0F, MAX_ARMOR);
		return damage * (DAMAGE_REDUCTION_DIVISOR - effectiveArmor) / DAMAGE_REDUCTION_DIVISOR;
	}

	/**
	 * Reproduces EnchantmentHelper#getEnchantmentModifierDamage followed by EntityLivingBase's EPF cap.
	 */
	public static float applyEnchantmentProtection(float damage, float protection) {
		int rawProtection = Mth.clamp(Mth.floor(protection), 0, MAX_RAW_ENCHANTMENT_PROTECTION);
		int randomizedProtection = ((rawProtection + 1) >> 1)
				+ PROTECTION_RANDOM.nextInt((rawProtection >> 1) + 1);
		int effectiveProtection = Math.min(randomizedProtection, MAX_EFFECTIVE_ENCHANTMENT_PROTECTION);
		return damage * (DAMAGE_REDUCTION_DIVISOR - effectiveProtection) / DAMAGE_REDUCTION_DIVISOR;
	}
}
