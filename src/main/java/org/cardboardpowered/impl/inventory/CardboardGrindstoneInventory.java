package org.cardboardpowered.impl.inventory;

import net.minecraft.inventory.Inventory;

import org.bukkit.craftbukkit.inventory.CraftResultInventory;
import org.bukkit.inventory.GrindstoneInventory;

public class CardboardGrindstoneInventory extends CraftResultInventory implements GrindstoneInventory {

    public CardboardGrindstoneInventory(Inventory inventory, Inventory resultInventory) {
        super(inventory, resultInventory);
    }

}