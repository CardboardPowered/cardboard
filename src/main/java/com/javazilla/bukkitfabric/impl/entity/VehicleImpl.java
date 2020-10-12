package com.javazilla.bukkitfabric.impl.entity;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Vehicle;

public abstract class VehicleImpl extends CraftEntity implements Vehicle {

    public VehicleImpl(CraftServer server, net.minecraft.entity.Entity entity) {
        super(entity);
    }

    @Override
    public String toString() {
        return "CraftVehicle{passenger=" + getPassenger() + '}';
    }

}