package com.javazilla.bukkitfabric.impl.inventory;

import net.minecraft.inventory.Inventory;

import org.bukkit.craftbukkit.inventory.CraftResultInventory;
import org.bukkit.inventory.SmithingInventory;

public class SmithingInventoryImpl extends CraftResultInventory implements SmithingInventory {

    public SmithingInventoryImpl(Inventory inventory, Inventory resultInventory) {
        super(inventory, resultInventory);
    }

}