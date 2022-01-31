package org.cardboardpowered.impl.entity;

import org.bukkit.Material;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Pig;

import net.minecraft.entity.passive.PigEntity;

public class CardboardPig extends AnimalsImpl implements Pig {

    public CardboardPig(CraftServer server, PigEntity entity) {
        super(server, entity);
    }

    @Override
    public boolean hasSaddle() {
        return this.getHandle().isSaddled();
    }

    @Override
    public void setSaddle(boolean saddled) {
        // TODO implement
    }

    @Override
    public int getBoostTicks() {
        // TODO implement
        return 0;
    }

    @Override
    public void setBoostTicks(int ticks) {
        // TODO implement
    }

    @Override
    public int getCurrentBoostTicks() {
        // TODO implement
        return 0;
    }

    @Override
    public void setCurrentBoostTicks(int ticks) {
        // TODO implement
    }

    @Override
    public Material getSteerMaterial() {
        return Material.CARROT_ON_A_STICK;
    }

    @Override
    public PigEntity getHandle() {
        return (PigEntity)this.nms;
    }

    @Override
    public String toString() {
        return "Pig";
    }

    @Override
    public EntityType getType() {
        return EntityType.PIG;
    }

}