/*
 Little Logistics: Quilt Edition, a mod about transportation for Minecraft
 Copyright © 2022 EDToaster, Murad Akhundov, LuaX, Abbie

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 3 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package de.luaxlab.shipping.common.block.dock;

import de.luaxlab.shipping.common.core.ModBlockEntities;
import de.luaxlab.shipping.common.entity.vessel.VesselEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class TugDockTileEntity extends AbstractHeadDockTileEntity<VesselEntity> {
    public TugDockTileEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TUG_DOCK.get(), pos, state);
    }

    @Override
    protected List<BlockPos> getTargetBlockPos() {
        return List.of(this.getBlockPos().above());
    }

    @Override
    protected boolean checkBadDirCondition(VesselEntity tug, Direction direction) {
        return !getBlockState().getValue(DockingBlockStates.FACING).getOpposite().equals(direction)
                ||
                tug.getDirection().equals(getRowDirection(getBlockState().getValue(DockingBlockStates.FACING)));
    }

    @Override
    protected Direction getRowDirection(Direction facing) {
        return this.getBlockState().getValue(DockingBlockStates.INVERTED) ? facing.getClockWise() : facing.getCounterClockWise();
    }

}
