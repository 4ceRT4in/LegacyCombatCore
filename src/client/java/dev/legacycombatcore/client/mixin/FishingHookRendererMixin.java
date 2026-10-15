package dev.legacycombatcore.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.FishingHookRenderer;
import net.minecraft.world.entity.projectile.FishingHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingHookRenderer.class)
abstract class FishingHookRendererMixin {
	@Inject(
		method = "render(Lnet/minecraft/world/entity/projectile/FishingHook;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
		at = @At("HEAD"),
		cancellable = true
	)
	private void legacyCombatCore$hideHookFromHookedPlayer(
			FishingHook fishingHook,
			float entityYaw,
			float partialTick,
			PoseStack poseStack,
			MultiBufferSource bufferSource,
			int packedLight,
			CallbackInfo callbackInfo
	) {
		// 1.21 positions a hooked bobber at 80% of the target's height. At first-
		// person camera distance its billboard can cover most of the victim's view.
		if (fishingHook.getHookedIn() == Minecraft.getInstance().player) {
			callbackInfo.cancel();
		}
	}
}
