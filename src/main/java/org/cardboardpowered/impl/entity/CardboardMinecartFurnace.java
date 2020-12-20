package org.cardboardpowered.impl.entity;

import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.minecart.PoweredMinecart;

public class CardboardMinecartFurnace extends CardboardMinecart implements PoweredMinecart {

    public CardboardMinecartFurnace(CraftServer server, FurnaceMinecartEntity entity) {
        super(server, entity);
    }

    @Override
    public String toString() {
        return "Furnacecart";
    }

    @Override
    public EntityType getType() {
        return EntityType.MINECART_FURNACE;
    }

    @Override
    public int getFuel() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setFuel(int fuel) {
        // TODO Auto-generated method stub
    }

}