package org.cardboardpowered.impl.entity;

import net.minecraft.entity.mob.PhantomEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Phantom;

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

}