package org.bukkit.craftbukkit.inventory;

import net.minecraft.inventory.Inventory;
import org.bukkit.inventory.SmithingInventory;

public class CraftInventorySmithing extends CraftResultInventory implements SmithingInventory {

    public CraftInventorySmithing(Inventory inventory, Inventory resultInventory) {
        super(inventory, resultInventory);
    }

}