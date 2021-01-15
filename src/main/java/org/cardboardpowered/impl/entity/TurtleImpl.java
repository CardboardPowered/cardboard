package org.cardboardpowered.impl.entity;

import net.minecraft.entity.passive.TurtleEntity;

import org.bukkit.Location;
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

    @Override
    public Location getHome() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean hasEgg() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isDigging() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isGoingHome() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setHasEgg(boolean arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setHome(Location arg0) {
        // TODO Auto-generated method stub
        
    }

}