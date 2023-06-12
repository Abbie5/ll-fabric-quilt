package de.luaxlab.shipping.common.extensions;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.quiltmc.qsl.base.api.util.InjectedInterface;

@InjectedInterface(BaseRailBlock.class)
public interface LLBaseRailBlockExtensions {
	default boolean canMakeSlopes(BlockState state, BlockGetter level, BlockPos pos) {
		return true;
	}
}
