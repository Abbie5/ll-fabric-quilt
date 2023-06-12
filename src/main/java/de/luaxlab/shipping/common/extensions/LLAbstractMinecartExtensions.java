package de.luaxlab.shipping.common.extensions;

import net.minecraft.world.entity.vehicle.AbstractMinecart;
import org.quiltmc.qsl.base.api.util.InjectedInterface;

@InjectedInterface(AbstractMinecart.class)
public interface LLAbstractMinecartExtensions {
	default boolean isPoweredCart() {
		throw new RuntimeException();
	}
}
