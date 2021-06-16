package org.cardboardpowered.impl.entity;

import net.minecraft.entity.passive.WanderingTraderEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.WanderingTrader;

public class WanderingTraderImpl extends AbstractVillagerImpl implements WanderingTrader {

    public WanderingTraderImpl(CraftServer server, WanderingTraderEntity entity) {
        super(server, entity);
    }

    @Override
    public WanderingTraderEntity getHandle() {
        return (WanderingTraderEntity) nms;
    }

    @Override
    public String toString() {
        return "WanderingTraderImpl";
    }

    @Override
    public EntityType getType() {
        return EntityType.WANDERING_TRADER;
    }

    @Override
    public boolean canDrinkMilk() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean canDrinkPotion() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int getDespawnDelay() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setCanDrinkMilk(boolean arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setCanDrinkPotion(boolean arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setDespawnDelay(int arg0) {
        // TODO Auto-generated method stub
        
    }

}