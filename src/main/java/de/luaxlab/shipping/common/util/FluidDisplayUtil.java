package de.luaxlab.shipping.common.util;

import io.github.fabricators_of_create.porting_lib.transfer.fluid.FluidTank;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class FluidDisplayUtil {
    public static Component getFluidDisplay(FluidTank tank) {
        Fluid fluid = tank.getFluid().getFluid();
        return fluid.equals(Fluids.EMPTY) ?
                Component.translatable("block.littlelogistics.fluid_hopper.capacity_empty", tank.getCapacity()) :
                Component.translatable("block.littlelogistics.fluid_hopper.capacity", tank.getFluid().getDisplayName().getString(),
                        tank.getFluidAmount(), tank.getCapacity());
    }
}
