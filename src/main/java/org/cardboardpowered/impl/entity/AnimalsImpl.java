package org.cardboardpowered.impl.entity;

import com.google.common.base.Preconditions;
import java.util.UUID;
import net.minecraft.entity.passive.AnimalEntity;

import org.bukkit.Material;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Animals;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

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
        return "Animal";
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

    // 1.17 API Start

    @Override
    public boolean isBreedItem(ItemStack itemStack) {
        return this.getHandle().isBreedingItem(CraftItemStack.asNMSCopy(itemStack));
    }

    @Override
    public boolean isBreedItem(Material material) {
        return this.isBreedItem(new ItemStack(material));
    }
}
