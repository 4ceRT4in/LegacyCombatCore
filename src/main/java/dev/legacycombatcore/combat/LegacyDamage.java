package dev.legacycombatcore.combat;

import java.util.Random;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

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
	public static float applyEnchantmentProtection(
			LivingEntity entity,
			DamageSource source,
			float damage
	) {
		int rawProtection = Mth.clamp(
				getRawEnchantmentProtection(entity, source),
				0,
				MAX_RAW_ENCHANTMENT_PROTECTION
		);
		int randomizedProtection = ((rawProtection + 1) >> 1)
				+ PROTECTION_RANDOM.nextInt((rawProtection >> 1) + 1);
		int effectiveProtection = Math.min(randomizedProtection, MAX_EFFECTIVE_ENCHANTMENT_PROTECTION);
		return damage * (DAMAGE_REDUCTION_DIVISOR - effectiveProtection) / DAMAGE_REDUCTION_DIVISOR;
	}

	private static int getRawEnchantmentProtection(LivingEntity entity, DamageSource source) {
		HolderLookup.RegistryLookup<Enchantment> enchantments = entity.registryAccess()
				.lookupOrThrow(Registries.ENCHANTMENT);
		Holder<Enchantment> protectionEnchantment = enchantments.getOrThrow(Enchantments.PROTECTION);
		Holder<Enchantment> fireProtectionEnchantment = enchantments.getOrThrow(Enchantments.FIRE_PROTECTION);
		Holder<Enchantment> featherFallingEnchantment = enchantments.getOrThrow(Enchantments.FEATHER_FALLING);
		Holder<Enchantment> blastProtectionEnchantment = enchantments.getOrThrow(Enchantments.BLAST_PROTECTION);
		Holder<Enchantment> projectileProtectionEnchantment = enchantments.getOrThrow(Enchantments.PROJECTILE_PROTECTION);
		int protection = 0;

		for (ItemStack armor : entity.getArmorSlots()) {
			protection += getProtectionContribution(
					armor,
					protectionEnchantment,
					0.75F
			);

			if (source.is(DamageTypeTags.IS_FIRE)) {
				protection += getProtectionContribution(
						armor,
						fireProtectionEnchantment,
						1.25F
				);
			}

			if (source.is(DamageTypes.FALL)) {
				protection += getProtectionContribution(
						armor,
						featherFallingEnchantment,
						2.5F
				);
			}

			if (source.is(DamageTypeTags.IS_EXPLOSION)) {
				protection += getProtectionContribution(
						armor,
						blastProtectionEnchantment,
						1.5F
				);
			}

			if (source.is(DamageTypeTags.IS_PROJECTILE)) {
				protection += getProtectionContribution(
						armor,
						projectileProtectionEnchantment,
						1.5F
				);
			}
		}

		return protection;
	}

	private static int getProtectionContribution(
			ItemStack stack,
			Holder<Enchantment> enchantment,
			float multiplier
	) {
		int level = EnchantmentHelper.getItemEnchantmentLevel(enchantment, stack);
		if (level <= 0) {
			return 0;
		}

		float baseProtection = (6.0F + level * level) / 3.0F;
		return Mth.floor(baseProtection * multiplier);
	}
}
