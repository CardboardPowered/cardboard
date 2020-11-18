package org.cardboardpowered.impl.entity;

import net.minecraft.entity.mob.IllagerEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Illager;

public class CardboardIllager extends CardboardRaider implements Illager {

    public CardboardIllager(CraftServer server, IllagerEntity entity) {
        super(server, entity);
    }

    @Override
    public IllagerEntity getHandle() {
        return (IllagerEntity) super.getHandle();
    }

    @Override
    public String toString() {
        return "Illager";
    }

}