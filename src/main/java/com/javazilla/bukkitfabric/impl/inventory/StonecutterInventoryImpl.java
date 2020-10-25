package com.javazilla.bukkitfabric.impl.inventory;

import net.minecraft.inventory.Inventory;

import org.bukkit.craftbukkit.inventory.CraftResultInventory;
import org.bukkit.inventory.StonecutterInventory;

public class StonecutterInventoryImpl extends CraftResultInventory implements StonecutterInventory {

    public StonecutterInventoryImpl(Inventory inventory, Inventory resultInventory) {
        super(inventory, resultInventory);
    }

}