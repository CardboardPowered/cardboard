package org.cardboardpowered.impl.entity;

import com.google.common.base.Preconditions;
import java.util.UUID;
import net.minecraft.entity.passive.AnimalEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Animals;

public class AnimalsImpl extends AgeableImpl implements Animals {

    public AnimalsImpl(CraftServer server, AnimalEntity entity) {
        super(server, entity);
    }

    @Override
    public AnimalEntity getHandle() {
        return (AnimalEntity) nms;
    }

    @Override
    public String toString() {
        return "CraftAnimals";
    }

    @Override
    public UUID getBreedCause() {
        // TODO return getHandle().lovingPlayer;
        return UUID.randomUUID();
    }

    @Override
    public void setBreedCause(UUID uuid) {
        // TODO getHandle().lovingPlayer = uuid;
    }

    @Override
    public boolean isLoveMode() {
        return getHandle().isInLove();
    }

    @Override
    public void setLoveModeTicks(int ticks) {
        Preconditions.checkArgument(ticks >= 0, "Love mode ticks must be positive or 0");
        getHandle().setLoveTicks(ticks);
    }

    @Override
    public int getLoveModeTicks() {
        return getHandle().getLoveTicks();
    }

}
