package org.cardboardpowered.impl.entity;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.minecart.RideableMinecart;

import net.minecraft.entity.vehicle.AbstractMinecartEntity;

public class CardboardMinecartRideable extends CardboardMinecart implements RideableMinecart {

    public CardboardMinecartRideable(CraftServer server, AbstractMinecartEntity entity) {
        super(server, entity);
    }

    @Override
    public String toString() {
        return "MinecartRideable";
    }

    @Override
    public EntityType getType() {
        return EntityType.MINECART;
    }

}