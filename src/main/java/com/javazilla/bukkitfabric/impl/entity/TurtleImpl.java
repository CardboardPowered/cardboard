package com.javazilla.bukkitfabric.impl.entity;

import net.minecraft.entity.passive.TurtleEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Turtle;

public class TurtleImpl extends AnimalsImpl implements Turtle {

    public TurtleImpl(CraftServer server, TurtleEntity entity) {
        super(server, entity);
    }

    @Override
    public TurtleEntity getHandle() {
        return (TurtleEntity) super.getHandle();
    }

    @Override
    public String toString() {
        return "FabricTurtle";
    }

    @Override
    public EntityType getType() {
        return EntityType.TURTLE;
    }

}