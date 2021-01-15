package org.cardboardpowered.impl.entity;

import net.minecraft.entity.vehicle.ChestMinecartEntity;

import java.util.UUID;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.inventory.Inventory;

public class CardboardMinecartChest extends CardboardMinecartSH implements StorageMinecart {

    public CraftInventory inventory;

    public CardboardMinecartChest(CraftServer server, ChestMinecartEntity entity) {
        super(server, entity);
        inventory = new CraftInventory(entity);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public String toString() {
        return "Minecart{" + "inventory=" + inventory + '}';
    }

    @Override
    public EntityType getType() {
        return EntityType.MINECART_CHEST;
    }

    @Override
    public Entity getEntity() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getLastFilled() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Long getLastLooted(UUID arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getNextRefill() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean hasBeenFilled() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean hasPendingRefill() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean hasPlayerLooted(UUID arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isRefillEnabled() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean setHasPlayerLooted(UUID arg0, boolean arg1) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public long setNextRefill(long arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

}