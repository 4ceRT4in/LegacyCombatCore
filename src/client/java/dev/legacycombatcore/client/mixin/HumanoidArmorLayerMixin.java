package dev.legacycombatcore.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.LivingEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
abstract class HumanoidArmorLayerMixin {
	@Unique
	private LivingEntity legacyCombatCore$renderedEntity;

	@Inject(method = "render", at = @At("HEAD"))
	private void legacyCombatCore$captureRenderedEntity(
			PoseStack poseStack,
			MultiBufferSource bufferSource,
			int packedLight,
			LivingEntity entity,
			float limbSwing,
			float limbSwingAmount,
			float partialTick,
			float ageInTicks,
			float netHeadYaw,
			float headPitch,
			CallbackInfo callbackInfo
	) {
		this.legacyCombatCore$renderedEntity = entity;
	}

	@Inject(method = "render", at = @At("RETURN"))
	private void legacyCombatCore$clearRenderedEntity(
			PoseStack poseStack,
			MultiBufferSource bufferSource,
			int packedLight,
			LivingEntity entity,
			float limbSwing,
			float limbSwingAmount,
			float partialTick,
			float ageInTicks,
			float netHeadYaw,
			float headPitch,
			CallbackInfo callbackInfo
	) {
		this.legacyCombatCore$renderedEntity = null;
	}

	@Redirect(
			method = "renderModel",
			at = @At(
					value = "FIELD",
					target = "Lnet/minecraft/client/renderer/texture/OverlayTexture;NO_OVERLAY:I",
					opcode = Opcodes.GETSTATIC
			)
	)
	private int legacyCombatCore$applyHurtOverlayToArmor() {
		LivingEntity entity = this.legacyCombatCore$renderedEntity;
		return entity != null
				? LivingEntityRenderer.getOverlayCoords(entity, 0.0F)
				: OverlayTexture.NO_OVERLAY;
	}
}
