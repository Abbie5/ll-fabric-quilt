package de.luaxlab.shipping.mixin;

import de.luaxlab.shipping.common.extensions.LLBaseRailBlockExtensions;
import net.minecraft.world.level.block.BaseRailBlock;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BaseRailBlock.class)
public abstract class BaseRailBlockMixin implements LLBaseRailBlockExtensions {
}
