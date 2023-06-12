package de.luaxlab.shipping.common.block.rail.blockentity;

import de.luaxlab.shipping.common.block.dock.AbstractTailDockTileEntity;
import de.luaxlab.shipping.common.block.rail.AbstractDockingRail;
import de.luaxlab.shipping.common.core.ModBlockEntities;
import de.luaxlab.shipping.common.entity.train.AbstractTrainCarEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;

import java.util.List;

public class TrainCarDockTileEntity extends AbstractTailDockTileEntity<AbstractTrainCarEntity> {

    public TrainCarDockTileEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CAR_DOCK.get(), pos, state);
    }

    @Override
    protected List<BlockPos> getTargetBlockPos() {
        if(this.isExtract()){
            return List.of(getBlockPos().below());
        }
        var facing = getBlockState().getValue(AbstractDockingRail.RAIL_SHAPE).equals(RailShape.EAST_WEST) ? Direction.EAST : Direction.NORTH;
        return List.of(getBlockPos().relative(facing.getCounterClockWise()), getBlockPos().relative(facing.getClockWise()));
    }

    @Override
    protected boolean checkBadDirCondition(Direction direction) {
        return false;
    }
}
