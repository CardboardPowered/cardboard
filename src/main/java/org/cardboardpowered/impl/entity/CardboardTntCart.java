package org.cardboardpowered.impl.entity;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.minecart.ExplosiveMinecart;

import net.minecraft.entity.vehicle.AbstractMinecartEntity;

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

}