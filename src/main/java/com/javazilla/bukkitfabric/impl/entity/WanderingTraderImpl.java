package com.javazilla.bukkitfabric.impl.entity;

import net.minecraft.entity.passive.WanderingTraderEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftAbstractVillager;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.WanderingTrader;

public class WanderingTraderImpl extends CraftAbstractVillager implements WanderingTrader {

    public WanderingTraderImpl(CraftServer server, WanderingTraderEntity entity) {
        super(server, entity);
    }

    @Override
    public WanderingTraderEntity getHandle() {
        return (WanderingTraderEntity) nms;
    }

    @Override
    public String toString() {
        return "WanderingTraderImpl";
    }

    @Override
    public EntityType getType() {
        return EntityType.WANDERING_TRADER;
    }

}