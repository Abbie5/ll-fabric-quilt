package de.luaxlab.shipping.common.block.rail.blockentity;

import de.luaxlab.shipping.common.block.dock.AbstractHeadDockTileEntity;
import de.luaxlab.shipping.common.block.dock.DockingBlockStates;
import de.luaxlab.shipping.common.core.ModBlockEntities;
import de.luaxlab.shipping.common.entity.train.AbstractTrainCarEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class LocomotiveDockTileEntity extends AbstractHeadDockTileEntity<AbstractTrainCarEntity> {
    public LocomotiveDockTileEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LOCOMOTIVE_DOCK.get(), pos, state);
    }

    @Override
    protected List<BlockPos> getTargetBlockPos() {
        var facing =  getBlockState().getValue(DockingBlockStates.FACING);
        return List.of(getBlockPos().relative(facing.getCounterClockWise()), getBlockPos().relative(facing.getClockWise()));
    }

    @Override
    protected boolean checkBadDirCondition(AbstractTrainCarEntity tug, Direction direction) {
        return !tug.getDirection().equals(getBlockState().getValue(DockingBlockStates.FACING));
    }

    @Override
    protected Direction getRowDirection(Direction facing) {
        return facing.getOpposite();
    }
}
