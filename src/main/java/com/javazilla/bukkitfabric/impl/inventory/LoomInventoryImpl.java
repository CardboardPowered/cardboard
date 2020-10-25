package com.javazilla.bukkitfabric.impl.inventory;

import net.minecraft.inventory.Inventory;

import org.bukkit.craftbukkit.inventory.CraftResultInventory;
import org.bukkit.inventory.LoomInventory;

public class LoomInventoryImpl extends CraftResultInventory implements LoomInventory {

    public LoomInventoryImpl(Inventory inventory, Inventory resultInventory) {
        super(inventory, resultInventory);
    }

}