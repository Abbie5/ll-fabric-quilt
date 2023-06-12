package de.luaxlab.shipping.common.entity.train.locomotive;

import de.luaxlab.shipping.common.core.ModComponents;
import de.luaxlab.shipping.common.core.ModConfig;
import de.luaxlab.shipping.common.core.ModEntities;
import de.luaxlab.shipping.common.core.ModItems;
import de.luaxlab.shipping.common.energy.EnergyUtils;
import de.luaxlab.shipping.common.energy.SimpleReadWriteEnergyStorage;
import de.luaxlab.shipping.common.entity.accessor.EnergyHeadVehicleDataAccessor;
import de.luaxlab.shipping.common.entity.container.EnergyHeadVehicleContainer;
import de.luaxlab.shipping.common.util.ItemHandlerVanillaContainerWrapper;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import io.github.fabricators_of_create.porting_lib.util.LazyOptional;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.EnergyStorageUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EnergyLocomotiveEntity extends AbstractLocomotiveEntity implements ItemHandlerVanillaContainerWrapper, WorldlyContainer {
    private final ItemStackHandler itemHandler = createHandler();
    private static final long MAX_ENERGY = ModConfig.Server.ENERGY_LOCO_BASE_CAPACITY.get();
    private static final long MAX_TRANSFER = ModConfig.Server.ENERGY_LOCO_BASE_MAX_CHARGE_RATE.get();
    private static final long ENERGY_USAGE = ModConfig.Server.ENERGY_LOCO_BASE_ENERGY_USAGE.get();

    private final SimpleReadWriteEnergyStorage internalBattery = new SimpleReadWriteEnergyStorage(MAX_ENERGY, MAX_TRANSFER, Integer.MAX_VALUE);

    public EnergyLocomotiveEntity(EntityType<?> type, Level p_38088_) {
        super(type, p_38088_);
        internalBattery.amount = 0;
    }

    public EnergyLocomotiveEntity(Level level, Double aDouble, Double aDouble1, Double aDouble2) {
        super(ModEntities.ENERGY_LOCOMOTIVE.get(), level, aDouble, aDouble1, aDouble2);
        internalBattery.amount = 0;
    }

    private ItemStackHandler createHandler() {
        return new ItemStackHandler(1) {
            @Override
            public boolean isItemValid(int slot, @Nonnull ItemVariant variant, long maxAmount) {
                return EnergyStorageUtil.isEnergyStorage(variant.toStack());
            }

            @Nonnull
            @Override
            public long insertSlot(int slot, @Nonnull ItemVariant stack, long maxAmount, TransactionContext transaction) {
                if (!isItemValid(slot, stack, maxAmount)) {
                    return 0;
                }

                return super.insertSlot(slot, stack, maxAmount, transaction);
            }
        };
    }

    @Override
    public void remove(RemovalReason r) {
        if(!this.level.isClientSide){
            Containers.dropContents(this.level, this, this);
        }
        super.remove(r);
    }

    @Override
    protected MenuProvider createContainerProvider() {
        return new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.translatable("entity.littlelogistics.energy_locomotive");
            }

            @Nullable
            @Override
            public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player player) {
                return new EnergyHeadVehicleContainer<EnergyLocomotiveEntity>(i, level, getDataAccessor(), playerInventory, player);
            }
        };
    }

    @Override
    public EnergyHeadVehicleDataAccessor getDataAccessor() {
        return (EnergyHeadVehicleDataAccessor) new EnergyHeadVehicleDataAccessor.Builder()
                .withEnergy(internalBattery::getAmount)
                .withCapacity(internalBattery::getCapacity)
                .withLit(() -> internalBattery.getAmount() > 0) // has energy
                .withId(this.getId())
                .withOn(() -> engineOn)
                .withRouteSize(() -> navigator.getRouteSize())
                .withVisitedSize(() -> navigator.getVisitedSize())
                .withCanMove(enrollmentHandler::mayMove)
                .build();
    }

    @Override
    public void tick() {
        // grab energy from capacitor
        if (!level.isClientSide) {
            EnergyStorage source = EnergyUtils.getEnergyCapabilityInSlot(0, itemHandler);
            if (source != null && source.supportsExtraction()) {
				EnergyStorageUtil.move(source, internalBattery, MAX_TRANSFER, null);
            }
        }

        super.tick();
    }

    @Override
    protected boolean tickFuel() {
        return internalBattery.consume(ENERGY_USAGE) > 0;
    }


    @Override
    public ItemStack getPickResult() {
        return new ItemStack(ModItems.ENERGY_LOCOMOTIVE.get());
    }

    @Override
    public ItemStackHandler getRawHandler() {
        return itemHandler;
    }

    @Override
    public int[] getSlotsForFace(Direction p_180463_1_) {
        return new int[]{0};
    }

    @Override
    public boolean canPlaceItemThroughFace(int p_180462_1_, ItemStack p_180462_2_, @Nullable Direction p_180462_3_) {
        return stalling.isDocked();
    }

    @Override
    public boolean canTakeItemThroughFace(int p_180461_1_, ItemStack p_180461_2_, Direction p_180461_3_) {
        return false;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        itemHandler.deserializeNBT(compound.getCompound("inv"));
        internalBattery.readAdditionalSaveData(compound.getCompound("energy_storage"));
        super.readAdditionalSaveData(compound);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        compound.put("inv", itemHandler.serializeNBT());
        CompoundTag energyNBT = new CompoundTag();
        internalBattery.addAdditionalSaveData(energyNBT);
        compound.put("energy_storage", energyNBT);
        super.addAdditionalSaveData(compound);
    }
}
