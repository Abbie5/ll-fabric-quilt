package de.luaxlab.shipping.mixin;

import de.luaxlab.shipping.common.extensions.LLAbstractMinecartExtensions;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractMinecart.class)
public abstract class AbstractMinecartMixin implements LLAbstractMinecartExtensions {
	@Shadow
	public abstract AbstractMinecart.Type getMinecartType();

	@Override
	public boolean isPoweredCart() {
		return this.getMinecartType() == AbstractMinecart.Type.FURNACE;
	}

	@Redirect(
		method = "push",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/vehicle/AbstractMinecart;getMinecartType()Lnet/minecraft/world/entity/vehicle/AbstractMinecart$Type;"
		)
	)
	private AbstractMinecart.Type ll$poweredCart(AbstractMinecart instance) {
		return isPoweredCart() ? AbstractMinecart.Type.FURNACE : null;
	}
}
