package com.javazilla.bukkitfabric.impl.entity;

import net.minecraft.entity.passive.OcelotEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Ocelot;

public class OcelotImpl extends AnimalsImpl implements Ocelot {
 
    public OcelotImpl(CraftServer server, OcelotEntity ocelot) {
        super(server, ocelot);
    }

    @Override
    public OcelotEntity getHandle() {
        return (OcelotEntity) nms;
    }

    @SuppressWarnings("deprecation")
    @Override
    public Type getCatType() {
        return Type.WILD_OCELOT;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setCatType(Type type) {
        throw new UnsupportedOperationException("Cats are now a different entity!");
    }

    @Override
    public String toString() {
        return "Ocelot";
    }

    @Override
    public EntityType getType() {
        return EntityType.OCELOT;
    }

}