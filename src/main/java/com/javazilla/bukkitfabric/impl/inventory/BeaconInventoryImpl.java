package com.javazilla.bukkitfabric.impl.inventory;

import net.minecraft.inventory.Inventory;

import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.inventory.BeaconInventory;
import org.bukkit.inventory.ItemStack;

public class BeaconInventoryImpl extends CraftInventory implements BeaconInventory {

    public BeaconInventoryImpl(Inventory beacon) {
        super(beacon);
    }

    @Override
    public void setItem(ItemStack item) {
        setItem(0, item);
    }

    @Override
    public ItemStack getItem() {
        return getItem(0);
    }

}