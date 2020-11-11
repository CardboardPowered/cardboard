package org.cardboardpowered.impl.entity;

import net.minecraft.entity.passive.GolemEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Golem;

public class CardboardGolem extends CreatureImpl implements Golem {

    public CardboardGolem(CraftServer server, GolemEntity entity) {
        super(server, entity);
    }

    @Override
    public GolemEntity getHandle() {
        return (GolemEntity) nms;
    }

    @Override
    public String toString() {
        return "Golem";
    }

}