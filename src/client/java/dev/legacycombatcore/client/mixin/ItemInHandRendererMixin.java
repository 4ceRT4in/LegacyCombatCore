package dev.legacycombatcore.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
abstract class ItemInHandRendererMixin {
	@Shadow
	public abstract void renderItem(
			LivingEntity entity,
			ItemStack itemStack,
			ItemDisplayContext displayContext,
			boolean leftHand,
			PoseStack poseStack,
			MultiBufferSource bufferSource,
			int packedLight
	);

	@Redirect(
			method = "tick",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/player/LocalPlayer;getAttackStrengthScale(F)F"
			)
	)
	private float legacyCombatCore$doNotLowerHandDuringAttackCooldown(LocalPlayer player, float partialTick) {
		return 1.0F;
	}

	@Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
	private void legacyCombatCore$renderLegacyBowPose(
			AbstractClientPlayer player,
			float partialTick,
			float pitch,
			InteractionHand hand,
			float swingProgress,
			ItemStack itemStack,
			float equipProgress,
			PoseStack poseStack,
			MultiBufferSource bufferSource,
			int packedLight,
			CallbackInfo callbackInfo
	) {
		if (player.isScoping()
				|| !player.isUsingItem()
				|| player.getUseItemRemainingTicks() <= 0
				|| player.getUsedItemHand() != hand
				|| itemStack.getUseAnimation() != UseAnim.BOW) {
			return;
		}

		HumanoidArm arm = hand == InteractionHand.MAIN_HAND
				? player.getMainArm()
				: player.getMainArm().getOpposite();
		int direction = arm == HumanoidArm.RIGHT ? 1 : -1;

		poseStack.pushPose();
		legacyCombatCore$applyLegacyFirstPersonTransform(poseStack, direction, equipProgress);
		legacyCombatCore$applyLegacyBowTransform(poseStack, direction, itemStack, player, partialTick);
		legacyCombatCore$applyLegacyBowModelTransform(poseStack, direction);
		this.renderItem(
				player,
				itemStack,
				ItemDisplayContext.NONE,
				direction < 0,
				poseStack,
				bufferSource,
				packedLight
		);
		poseStack.popPose();
		callbackInfo.cancel();
	}

	private static void legacyCombatCore$applyLegacyFirstPersonTransform(
			PoseStack poseStack,
			int direction,
			float equipProgress
	) {
		poseStack.translate(direction * 0.56F, -0.52F - equipProgress * 0.6F, -0.72F);
		poseStack.mulPose(Axis.YP.rotationDegrees(direction * 45.0F));
		poseStack.scale(0.4F, 0.4F, 0.4F);
	}

	private static void legacyCombatCore$applyLegacyBowTransform(
			PoseStack poseStack,
			int direction,
			ItemStack itemStack,
			AbstractClientPlayer player,
			float partialTick
	) {
		poseStack.mulPose(Axis.ZP.rotationDegrees(direction * -18.0F));
		poseStack.mulPose(Axis.YP.rotationDegrees(direction * -12.0F));
		poseStack.mulPose(Axis.XP.rotationDegrees(-8.0F));
		poseStack.translate(direction * -0.9F, 0.2F, 0.0F);

		float useTicks = itemStack.getUseDuration(player)
				- (player.getUseItemRemainingTicks() - partialTick + 1.0F);
		float charge = useTicks / 20.0F;
		charge = (charge * charge + charge * 2.0F) / 3.0F;
		charge = Math.min(charge, 1.0F);

		if (charge > 0.1F) {
			float shake = Mth.sin((useTicks - 0.1F) * 1.3F) * (charge - 0.1F);
			poseStack.translate(0.0F, shake * 0.01F, 0.0F);
		}

		poseStack.translate(0.0F, 0.0F, charge * 0.1F);
		poseStack.scale(1.0F, 1.0F, 1.0F + charge * 0.2F);
	}

	private static void legacyCombatCore$applyLegacyBowModelTransform(PoseStack poseStack, int direction) {
		// 1.8.9 scales flat/generated models up before applying their camera transform, then
		// scales the baked model itself back down while rendering. The scales cancel for the
		// geometry, but deliberately double the camera translation in between.
		poseStack.scale(2.0F, 2.0F, 2.0F);
		poseStack.translate(0.0F, 4.0F / 16.0F, 2.0F / 16.0F);
		poseStack.mulPose(Axis.YP.rotationDegrees(direction * -135.0F));
		poseStack.mulPose(Axis.ZP.rotationDegrees(direction * 25.0F));
		poseStack.scale(1.7F, 1.7F, 1.7F);
		poseStack.scale(0.5F, 0.5F, 0.5F);
	}
}
