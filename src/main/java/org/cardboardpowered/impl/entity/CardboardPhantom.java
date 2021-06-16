package org.cardboardpowered.impl.entity;

import net.minecraft.entity.mob.PhantomEntity;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Phantom;
import org.jetbrains.annotations.NotNull;

public class CardboardPhantom extends CardboardFlying implements Phantom {

    public CardboardPhantom(CraftServer server, PhantomEntity entity) {
        super(server, entity);
    }

    @Override
    public PhantomEntity getHandle() {
        return (PhantomEntity) super.getHandle();
    }

    @Override
    public int getSize() {
        return getHandle().getPhantomSize();
    }

    @Override
    public void setSize(int sz) {
        getHandle().setPhantomSize(sz);
    }

    @Override
    public String toString() {
        return "Phantom";
    }

    @Override
    public EntityType getType() {
        return EntityType.PHANTOM;
    }

    @Override
    public UUID getSpawningEntity() {
        // TODO Auto-generated method stub
        return getHandle().getUuid();
    }

    @Override
    public int getHeadRotationSpeed() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getMaxHeadPitch() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void lookAt(@NotNull Location arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void lookAt(@NotNull Entity arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void lookAt(@NotNull Location arg0, float arg1, float arg2) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void lookAt(@NotNull Entity arg0, float arg1, float arg2) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void lookAt(double arg0, double arg1, double arg2) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void lookAt(double arg0, double arg1, double arg2, float arg3, float arg4) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setShouldBurnInDay(boolean arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean shouldBurnInDay() {
        // TODO Auto-generated method stub
        return false;
    }

}