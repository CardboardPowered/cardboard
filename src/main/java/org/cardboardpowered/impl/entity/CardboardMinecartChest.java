package org.cardboardpowered.impl.entity;

import net.minecraft.entity.vehicle.ChestMinecartEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.inventory.CraftInventory;
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

}