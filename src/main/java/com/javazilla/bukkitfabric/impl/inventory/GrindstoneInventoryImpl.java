package com.javazilla.bukkitfabric.impl.inventory;

import net.minecraft.inventory.Inventory;

import org.bukkit.craftbukkit.inventory.CraftResultInventory;
import org.bukkit.inventory.GrindstoneInventory;

public class GrindstoneInventoryImpl extends CraftResultInventory implements GrindstoneInventory {

    public GrindstoneInventoryImpl(Inventory inventory, Inventory resultInventory) {
        super(inventory, resultInventory);
    }

}