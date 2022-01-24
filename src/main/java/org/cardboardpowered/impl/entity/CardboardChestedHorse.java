package org.cardboardpowered.impl.entity;

import net.minecraft.entity.passive.AbstractDonkeyEntity;

import java.util.UUID;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.ChestedHorse;
import org.jetbrains.annotations.Nullable;

public abstract class CardboardChestedHorse extends CardboardAbstractHorse implements ChestedHorse {

    public CardboardChestedHorse(CraftServer server, AbstractDonkeyEntity entity) {
        super(server, entity);
    }

    @Override
    public AbstractDonkeyEntity getHandle() {
        return (AbstractDonkeyEntity)super.getHandle();
    }

    @Override
    public boolean isCarryingChest() {
        return this.getHandle().hasChest();
    }

    @Override
    public void setCarryingChest(boolean chest) {
        if (chest == isCarryingChest()) return;

        this.getHandle().setHasChest(chest);
        // this.getHandle().onChestedStatusChanged();
    }

    @Override
    public boolean isEating() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isEatingGrass() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isRearing() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setEating(boolean bl) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setEatingGrass(boolean bl) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setRearing(boolean bl) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public @Nullable UUID getOwnerUniqueId() {
        // TODO Auto-generated method stub
        return null;
    }

}