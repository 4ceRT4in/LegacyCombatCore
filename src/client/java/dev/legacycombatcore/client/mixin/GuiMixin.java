package dev.legacycombatcore.client.mixin;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Gui.class)
abstract class GuiMixin {
	@Redirect(
			method = {"renderCrosshair", "renderItemHotbar"},
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V"
			)
	)
	private void legacyCombatCore$hideAttackIndicatorSprite(
			GuiGraphics graphics,
			ResourceLocation sprite,
			int x,
			int y,
			int width,
			int height
	) {
		if (!legacyCombatCore$isAttackIndicator(sprite)) {
			graphics.blitSprite(sprite, x, y, width, height);
		}
	}

	@Redirect(
			method = {"renderCrosshair", "renderItemHotbar"},
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIIIIIII)V"
			)
	)
	private void legacyCombatCore$hideAttackIndicatorProgressSprite(
			GuiGraphics graphics,
			ResourceLocation sprite,
			int textureWidth,
			int textureHeight,
			int u,
			int v,
			int x,
			int y,
			int width,
			int height
	) {
		if (!legacyCombatCore$isAttackIndicator(sprite)) {
			graphics.blitSprite(sprite, textureWidth, textureHeight, u, v, x, y, width, height);
		}
	}

	private static boolean legacyCombatCore$isAttackIndicator(ResourceLocation sprite) {
		if (!ResourceLocation.DEFAULT_NAMESPACE.equals(sprite.getNamespace())) {
			return false;
		}

		String path = sprite.getPath();
		return path.startsWith("hud/crosshair_attack_indicator_")
				|| path.startsWith("hud/hotbar_attack_indicator_");
	}
}
