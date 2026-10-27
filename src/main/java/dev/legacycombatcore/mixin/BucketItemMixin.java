package dev.legacycombatcore.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BucketItem.class)
abstract class BucketItemMixin {
	@Redirect(
			method = "use",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/item/BucketItem;emptyContents(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/BlockHitResult;)Z"
			)
	)
	private boolean legacyCombatCore$placeWaterAboveWaterloggableBlocks(
			BucketItem bucket,
			Player player,
			Level level,
			BlockPos targetPos,
			BlockHitResult hitResult
	) {
		BlockPos hitPos = hitResult.getBlockPos();
		BlockState hitState = level.getBlockState(hitPos);
		if (bucket == Items.WATER_BUCKET && hitState.getBlock() instanceof LiquidBlockContainer) {
			// Before waterlogging existed, a water bucket targeted the free block above
			// blocks such as slabs, stairs and fences instead of filling the block itself.
			return bucket.emptyContents(player, level, hitPos.above(), null);
		}

		if (legacyCombatCore$isPlainFluidBucket(bucket) && legacyCombatCore$isReplaceablePlant(hitState)) {
			// Target the plant itself instead of the adjacent block selected by the
			// modern bucket code. emptyContents then replaces it with the fluid source.
			return bucket.emptyContents(player, level, hitPos, null);
		}

		return bucket.emptyContents(player, level, targetPos, hitResult);
	}

	@Unique
	private static boolean legacyCombatCore$isPlainFluidBucket(BucketItem bucket) {
		return bucket == Items.WATER_BUCKET || bucket == Items.LAVA_BUCKET;
	}

	@Unique
	private static boolean legacyCombatCore$isReplaceablePlant(BlockState state) {
		return state.is(Blocks.SHORT_GRASS) || state.is(Blocks.FERN) || state.is(Blocks.DEAD_BUSH);
	}
}
