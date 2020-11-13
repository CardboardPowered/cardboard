package org.cardboardpowered.impl.inventory;

import net.minecraft.inventory.Inventory;

import org.bukkit.craftbukkit.inventory.CraftResultInventory;
import org.bukkit.inventory.SmithingInventory;

public class CardboardSmithingInventory extends CraftResultInventory implements SmithingInventory {

    public CardboardSmithingInventory(Inventory inventory, Inventory resultInventory) {
        super(inventory, resultInventory);
    }

}