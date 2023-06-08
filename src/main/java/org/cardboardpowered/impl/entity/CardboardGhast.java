package org.cardboardpowered.impl.entity;

import net.minecraft.entity.mob.GhastEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Ghast;

public class CardboardGhast extends CardboardFlyingEntity implements Ghast {

    public CardboardGhast(CraftServer server, GhastEntity entity) {
        super(server, entity);
    }

    @Override
    public GhastEntity getHandle() {
        return (GhastEntity) nms;
    }

    @Override
    public String toString() {
        return "Ghast";
    }

    @Override
    public EntityType getType() {
        return EntityType.GHAST;
    }

	@Override
	public int getExplosionPower() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isCharging() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setCharging(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setExplosionPower(int arg0) {
		// TODO Auto-generated method stub
		
	}

}