package org.bukkit.craftbukkit.inventory;

import net.minecraft.world.Container;
import org.bukkit.inventory.CrafterInventory;

public class CraftInventoryCrafter extends CraftInventory implements CrafterInventory {

    public CraftInventoryCrafter(Container inventory) {
        super(inventory);
    }
}
