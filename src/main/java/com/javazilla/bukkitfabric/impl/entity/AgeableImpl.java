package com.javazilla.bukkitfabric.impl.entity;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Ageable;

import net.minecraft.entity.passive.PassiveEntity;

public class AgeableImpl extends CreatureImpl implements Ageable {

    public AgeableImpl(CraftServer server, PassiveEntity entity) {
        super(server, entity);
    }

    @Override
    public int getAge() {
        return getHandle().getBreedingAge();
    }

    @Override
    public void setAge(int age) {
        getHandle().setBreedingAge(age);
    }

    @Override
    public void setAgeLock(boolean lock) {
        // TODO
    }

    @Override
    public boolean getAgeLock() {
        return false; // TODO
    }

    @Override
    public void setBaby() {
        if (isAdult()) setAge(-24000);
    }

    @Override
    public void setAdult() {
        if (!isAdult()) setAge(0);
    }

    @Override
    public boolean isAdult() {
        return getAge() >= 0;
    }


    @Override
    public boolean canBreed() {
        return getAge() == 0;
    }

    @Override
    public void setBreed(boolean breed) {
        if (breed) setAge(0);
        else if (isAdult()) setAge(6000);
    }

    @Override
    public PassiveEntity getHandle() {
        return (PassiveEntity) nms;
    }

    @Override
    public String toString() {
        return "CraftAgeable";
    }

}