package org.cardboardpowered.impl.entity;

import net.minecraft.entity.mob.VindicatorEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Vindicator;

public class CardboardVindicator extends CardboardIllager implements Vindicator {

    public CardboardVindicator(CraftServer server, VindicatorEntity entity) {
        super(server, entity);
    }

    @Override
    public VindicatorEntity getHandle() {
        return (VindicatorEntity) super.getHandle();
    }

    @Override
    public String toString() {
        return "Vindicator";
    }

    @Override
    public EntityType getType() {
        return EntityType.VINDICATOR;
    }

    @Override
    public boolean isJohnny() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setJohnny(boolean arg0) {
        // TODO Auto-generated method stub
        
    }

}