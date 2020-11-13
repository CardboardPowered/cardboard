package org.cardboardpowered.impl.inventory;

import net.minecraft.inventory.Inventory;

import org.bukkit.craftbukkit.inventory.CraftResultInventory;
import org.bukkit.inventory.LoomInventory;

public class CardboardLoomInventory extends CraftResultInventory implements LoomInventory {

    public CardboardLoomInventory(Inventory inventory, Inventory resultInventory) {
        super(inventory, resultInventory);
    }

}