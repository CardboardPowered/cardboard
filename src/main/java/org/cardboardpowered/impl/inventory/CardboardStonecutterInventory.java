package org.cardboardpowered.impl.inventory;

import net.minecraft.inventory.Inventory;

import org.bukkit.craftbukkit.inventory.CraftResultInventory;
import org.bukkit.inventory.StonecutterInventory;

public class CardboardStonecutterInventory extends CraftResultInventory implements StonecutterInventory {

    public CardboardStonecutterInventory(Inventory inventory, Inventory resultInventory) {
        super(inventory, resultInventory);
    }

}