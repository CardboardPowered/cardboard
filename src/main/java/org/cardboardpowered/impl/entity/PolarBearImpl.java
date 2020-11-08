package org.cardboardpowered.impl.entity;

import net.minecraft.entity.passive.PolarBearEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.PolarBear;

public class PolarBearImpl extends AnimalsImpl implements PolarBear {

    public PolarBearImpl(CraftServer server, PolarBearEntity entity) {
        super(server, entity);
    }

    @Override
    public PolarBearEntity getHandle() {
        return (PolarBearEntity) nms;
    }

    @Override
    public String toString() {
        return "FabricPolarBear";
    }

    @Override
    public EntityType getType() {
        return EntityType.POLAR_BEAR;
    }

}