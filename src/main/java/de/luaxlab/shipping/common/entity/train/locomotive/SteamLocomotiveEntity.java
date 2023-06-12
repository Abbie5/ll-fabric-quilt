package de.luaxlab.shipping.common.entity.train.locomotive;

import de.luaxlab.shipping.common.core.ModConfig;
import de.luaxlab.shipping.common.core.ModEntities;
import de.luaxlab.shipping.common.core.ModItems;
import de.luaxlab.shipping.common.core.ModSounds;
import de.luaxlab.shipping.common.entity.accessor.SteamHeadVehicleDataAccessor;
import de.luaxlab.shipping.common.entity.container.SteamHeadVehicleContainer;
import de.luaxlab.shipping.common.entity.vessel.tug.AbstractTugEntity;
import de.luaxlab.shipping.common.util.ItemHandlerVanillaContainerWrapper;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SteamLocomotiveEntity extends AbstractLocomotiveEntity implements ItemHandlerVanillaContainerWrapper, WorldlyContainer {
	private final ItemStackHandler itemHandler = createHandler();
	private static final int FURNACE_FUEL_MULTIPLIER = ModConfig.Server.STEAM_LOCO_FUEL_MULTIPLIER.get();

	protected int burnTime = 0;
	protected int burnCapacity = 0;

	private ItemStackHandler createHandler() {
		return new ItemStackHandler(1) {
			@Override
			public boolean isItemValid(int slot, @Nonnull ItemVariant stack, long amount) {
				return FurnaceBlockEntity.isFuel(stack.toStack());
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

	public boolean isLit() {
		return burnTime > 0;
	}

	public int getBurnProgress() {
		int i = burnCapacity;
		if (i == 0) {
			i = 200;
		}

		return burnTime * 13 / i;
	}


	@Override
	public SteamHeadVehicleDataAccessor getDataAccessor() {
		return (SteamHeadVehicleDataAccessor) new SteamHeadVehicleDataAccessor.Builder()
			.withBurnProgress(this::getBurnProgress)
			.withId(this.getId())
			.withOn(() -> engineOn)
			.withRouteSize(() -> navigator.getRouteSize())
			.withVisitedSize(() -> navigator.getVisitedSize())
			.withLit(this::isLit)
			.withCanMove(enrollmentHandler::mayMove)
			.build();
	}

	@Override
	protected boolean tickFuel() {
		if (burnTime > 0) {
			burnTime--;
			return true;
		} else {
			ItemStack stack = itemHandler.getStackInSlot(0);
			if (!stack.isEmpty()) {
				burnCapacity = (AbstractFurnaceBlockEntity.getFuel().getOrDefault(stack.getItem(), 0) * FURNACE_FUEL_MULTIPLIER) - 1;
				burnTime = burnCapacity - 1;
				stack.shrink(1);
				return true;
			} else {
				burnCapacity = 0;
				burnTime = 0;
				return false;
			}
		}
	}

	@Override
	protected void onUndock() {
		super.onUndock();
		this.playSound(ModSounds.STEAM_TUG_WHISTLE.get(), 1, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
	}

	@Override
	protected MenuProvider createContainerProvider() {
		return new MenuProvider() {
			@Override
			public Component getDisplayName() {
				return Component.translatable("entity.littlelogistics.steam_locomotive");
			}

			@Nullable
			@Override
			public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player player) {
				return new SteamHeadVehicleContainer(i, level, getDataAccessor(), playerInventory, player);
			}
		};
	}

	@Override
	public void remove(RemovalReason r) {
		if (!this.level.isClientSide) {
			Containers.dropContents(this.level, this, this);
		}
		super.remove(r);
	}

	public SteamLocomotiveEntity(EntityType<?> type, Level p_38088_) {
		super(type, p_38088_);
	}

	public SteamLocomotiveEntity(Level level, Double aDouble, Double aDouble1, Double aDouble2) {
		super(ModEntities.STEAM_LOCOMOTIVE.get(), level, aDouble, aDouble1, aDouble2);
	}


	@Override
	public ItemStack getPickResult() {
		return new ItemStack(ModItems.STEAM_LOCOMOTIVE.get());
	}


	protected void doMovementEffect() {
		Level world = this.level;
		if (world != null) {
			BlockPos blockpos = this.getOnPos().above().above();
			RandomSource random = world.random;
			if (random.nextFloat() < ModConfig.Client.LOCO_SMOKE_MODIFIER.get()) {
				for (int i = 0; i < random.nextInt(2) + 2; ++i) {
					AbstractTugEntity.makeParticles(world, blockpos, true, false);
				}
			}
		}
	}

	@Override
	public boolean canTakeItemThroughFace(int p_180461_1_, ItemStack p_180461_2_, Direction p_180461_3_) {
		return false;
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
	public ItemStackHandler getRawHandler() {
		return itemHandler;
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		itemHandler.deserializeNBT(compound.getCompound("inv"));
		burnTime = compound.contains("burn") ? compound.getInt("burn") : 0;
		burnCapacity = compound.contains("burn_capacity") ? compound.getInt("burn_capacity") : 0;
		super.readAdditionalSaveData(compound);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		compound.put("inv", itemHandler.serializeNBT());
		compound.putInt("burn", burnTime);
		compound.putInt("burn_capacity", burnCapacity);
		super.addAdditionalSaveData(compound);
	}
}
