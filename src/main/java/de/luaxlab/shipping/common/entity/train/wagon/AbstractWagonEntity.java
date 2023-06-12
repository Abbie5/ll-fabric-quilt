package de.luaxlab.shipping.common.entity.train.wagon;

import de.luaxlab.shipping.common.component.StallingComponent;
import de.luaxlab.shipping.common.core.ModComponents;
import de.luaxlab.shipping.common.entity.train.AbstractTrainCarEntity;
import de.luaxlab.shipping.common.entity.train.locomotive.AbstractLocomotiveEntity;
import de.luaxlab.shipping.common.util.Train;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.Optional;

public abstract class AbstractWagonEntity extends AbstractTrainCarEntity {

    public AbstractWagonEntity(EntityType<?> p_38087_, Level p_38088_) {
        super(p_38087_, p_38088_);
    }

    public AbstractWagonEntity(EntityType<?> p_38087_, Level level, Double aDouble, Double aDouble1, Double aDouble2) {
        super(p_38087_, level, aDouble, aDouble1, aDouble2);
    }

    @Override
    public void setDominated(AbstractTrainCarEntity entity) {
        linkingHandler.dominated = Optional.of(entity);
    }


    @Override
    public void setDominant(AbstractTrainCarEntity entity) {
        this.setTrain(entity.getTrain());
        linkingHandler.dominant = Optional.of(entity);
    }

    @Override
    public void tick() {
        if(capability.isFrozen() || linkingHandler.train.getTug().map(s -> (AbstractLocomotiveEntity) s).map(AbstractLocomotiveEntity::shouldFreezeTrain).orElse(false)){
            this.setDeltaMovement(Vec3.ZERO);
        } else {
            super.tick();
        }
    }

    @Override
    public void removeDominated() {
        if(!this.isAlive()){
            return;
        }
        linkingHandler.dominated = Optional.empty();
        linkingHandler.train.setTail(this);
    }

    @Override
    public void removeDominant() {
        if(!this.isAlive()){
            return;
        }
        linkingHandler.dominant = Optional.empty();
        this.setTrain(new Train<>(this));
    }

    @Override
    public void setTrain(Train<AbstractTrainCarEntity> train) {
        linkingHandler.train = train;
        train.setTail(this);
        linkingHandler.dominated.ifPresent(dominated -> {
            // avoid recursion loops
            if(!dominated.getTrain().equals(train)){
                dominated.setTrain(train);
            }
        });
    }

    // hack to disable hoppers
    public boolean isDockable() {
        return linkingHandler.dominant.map(dom -> this.distanceToSqr(dom) < 1.05).orElse(true);
    }


    public boolean allowDockInterface(){
        return isDockable();
    }

    private final StallingComponent capability = new StallingComponent() {
        @Override
        public boolean isDocked() {
            return delegate().map(StallingComponent::isDocked).orElse(false);
        }

        @Override
        public void dock(double x, double y, double z) {
            delegate().ifPresent(s -> s.dock(x, y, z));
        }

        @Override
        public void undock() {
            delegate().ifPresent(StallingComponent::undock);
        }

        @Override
        public boolean isStalled() {
            return delegate().map(StallingComponent::isStalled).orElse(false);
        }

        @Override
        public void stall() {
            delegate().ifPresent(StallingComponent::stall);
        }

        @Override
        public void unstall() {
            delegate().ifPresent(StallingComponent::unstall);
        }

        @Override
        public boolean isFrozen() {
            return AbstractWagonEntity.super.isFrozen();
        }

        @Override
        public void freeze() {
            AbstractWagonEntity.super.setFrozen(true);
        }

        @Override
        public void unfreeze() {
            AbstractWagonEntity.super.setFrozen(false);
        }

        private Optional<StallingComponent> delegate() {
            if (linkingHandler.train.getHead() instanceof AbstractLocomotiveEntity e) {
				return ModComponents.STALLING.maybeGet(e);
            }
            return Optional.empty();
        }
    };
}
