package de.luaxlab.shipping.mixin;

import de.luaxlab.shipping.common.extensions.LLBaseRailBlockExtensions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.RailState;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RailState.class)
public abstract class RailStateMixin {
	@Unique
	private boolean canMakeSlopes;

	@Shadow
	@Final
	private BaseRailBlock block;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void ll$addCanMakeSlopes(Level level, BlockPos pos, BlockState state, CallbackInfo ci) {
		this.canMakeSlopes = ((LLBaseRailBlockExtensions)this.block).canMakeSlopes(state, level, pos);
	}

	@Redirect(
		method = "connectTo",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/block/BaseRailBlock;isRail(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Z"
		)
	)
	private boolean ll$connectSlopes(Level level, BlockPos pos) {
		if (!this.canMakeSlopes) return false;
		return BaseRailBlock.isRail(level, pos);
	}

	@Redirect(
		method = "place",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/block/BaseRailBlock;isRail(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Z"
		)
	)
	private boolean ll$placeSlopes(Level level, BlockPos pos) {
		if (!this.canMakeSlopes) return false;
		return BaseRailBlock.isRail(level, pos);
	}
}
