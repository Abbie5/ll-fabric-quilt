package de.luaxlab.shipping.common.entity.train.wagon;

import de.luaxlab.shipping.common.core.ModEntities;
import de.luaxlab.shipping.common.core.ModItems;
import de.luaxlab.shipping.common.util.ItemHandlerVanillaContainerWrapper;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import io.github.fabricators_of_create.porting_lib.util.LazyOptional;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.stream.IntStream;

public class ChestCarEntity extends AbstractWagonEntity implements ItemHandlerVanillaContainerWrapper, WorldlyContainer, MenuProvider {
    protected final ItemStackHandler itemHandler = createHandler();
    public ChestCarEntity(EntityType<ChestCarEntity> p_38087_, Level p_38088_) {
        super(p_38087_, p_38088_);
    }

    public ChestCarEntity(Level level, Double aDouble, Double aDouble1, Double aDouble2) {
        super(ModEntities.CHEST_CAR.get(), level, aDouble, aDouble1, aDouble2);

    }

    @Override
    public void remove(RemovalReason r) {
        if (!this.level.isClientSide) {
            Containers.dropContents(this.level, this, this);
        }
        super.remove(r);
    }

    private ItemStackHandler createHandler() {
        return new ItemStackHandler(27);
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(ModItems.CHEST_CAR.get());
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand){
        if(!this.level.isClientSide){
            player.openMenu(this);
        }
        return InteractionResult.CONSUME;
    }

    @Nullable
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory, Player pPlayer) {
        if (pPlayer.isSpectator()) {
            return null;
        } else {
            return ChestMenu.threeRows(pContainerId, pInventory, this);
        }
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        if (this.isRemoved()) {
            return false;
        } else {
            return !(this.distanceToSqr(pPlayer) > 64.0D);
        }
    }


    @Override
    public ItemStackHandler getRawHandler() {
        return itemHandler;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag t) {
        super.addAdditionalSaveData(t);
        t.put("inv", itemHandler.serializeNBT());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag t) {
        super.readAdditionalSaveData(t);
        itemHandler.deserializeNBT(t.getCompound("inv"));
    }


    // hack to disable hoppers before docking complete

    @Override
    public int[] getSlotsForFace(Direction p_180463_1_) {
        return IntStream.range(0, getContainerSize()).toArray();
    }

    @Override
    public boolean canPlaceItemThroughFace(int p_180462_1_, ItemStack p_180462_2_, @Nullable Direction p_180462_3_) {
        return isDockable();
    }

    @Override
    public boolean canTakeItemThroughFace(int p_180461_1_, ItemStack p_180461_2_, Direction p_180461_3_) {
        return isDockable();
    }
}
