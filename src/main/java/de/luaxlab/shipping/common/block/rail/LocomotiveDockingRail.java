package de.luaxlab.shipping.common.block.rail;

import de.luaxlab.shipping.common.block.dock.DockingBlockStates;
import de.luaxlab.shipping.common.core.ModBlockEntities;
import io.github.fabricators_of_create.porting_lib.block.ConnectableRedstoneBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jetbrains.annotations.Nullable;

public class LocomotiveDockingRail extends AbstractDockingRail implements ConnectableRedstoneBlock {
    public LocomotiveDockingRail(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return super.getStateForPlacement(pContext)
                .setValue(DockingBlockStates.POWERED, pContext.getLevel().hasNeighborSignal(pContext.getClickedPos()))
                .setValue(DockingBlockStates.FACING, pContext.getHorizontalDirection());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(DockingBlockStates.POWERED, DockingBlockStates.FACING);
    }


    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block p_220069_4_, BlockPos p_220069_5_, boolean p_220069_6_) {
        super.neighborChanged(state, world, pos, p_220069_4_, p_220069_5_, p_220069_6_);
        if (!world.isClientSide) {
            boolean flag = state.getValue(DockingBlockStates.POWERED);
            if (flag != world.hasNeighborSignal(pos)) {
                world.setBlock(pos, state.cycle(DockingBlockStates.POWERED), 2);
            }
        }
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, @javax.annotation.Nullable Direction side) {
        return true;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return ModBlockEntities.LOCOMOTIVE_DOCK.get().create(pPos, pState);
    }
}
