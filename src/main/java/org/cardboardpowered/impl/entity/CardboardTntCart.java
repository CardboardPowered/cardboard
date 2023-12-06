package org.cardboardpowered.impl.entity;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.minecart.ExplosiveMinecart;

import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.TntMinecartEntity;

public class CardboardTntCart extends CardboardMinecart implements ExplosiveMinecart {

    public CardboardTntCart(CraftServer server, AbstractMinecartEntity entity) {
        super(server, entity);
    }

    @Override
    public EntityType getType() {
        return EntityType.MINECART_TNT;
    }

	@Override
    public int getFuseTicks() {
        return this.getHandle().getFireTicks();
    }

	@Override
	public void setFuseTicks(int arg0) {
        this.getHandle().fireTicks = arg0;
	}
	
	// 1.19.4:
	
    @Override
    public TntMinecartEntity getHandle() {
        return (TntMinecartEntity)super.getHandle();
    }

	@Override
	public void explode() {
        this.getHandle().explode(this.getHandle().getVelocity().horizontalLengthSquared());
	}

	@Override
	public void explode(double arg0) {
        this.getHandle().explode(arg0);
	}

	@Override
	public void ignite() {
        this.getHandle().prime();
	}

	@Override
	public boolean isIgnited() {
        return this.getHandle().isPrimed();
	}

}