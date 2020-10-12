package com.javazilla.bukkitfabric.impl.entity;

import net.minecraft.entity.mob.EndermiteEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Endermite;
import org.bukkit.entity.EntityType;

public class EndermiteImpl extends MonsterImpl implements Endermite {

    public EndermiteImpl(CraftServer server, EndermiteEntity entity) {
        super(server, entity);
    }

    @Override
    public EndermiteEntity getHandle() {
        return (EndermiteEntity) super.getHandle();
    }

    @Override
    public String toString() {
        return "EndermiteImpl";
    }

    @Override
    public EntityType getType() {
        return EntityType.ENDERMITE;
    }

    @Override
    public boolean isPlayerSpawned() {
        return getHandle().isPlayerSpawned();
    }

    @Override
    public void setPlayerSpawned(boolean playerSpawned) {
        getHandle().setPlayerSpawned(playerSpawned);
    }

}