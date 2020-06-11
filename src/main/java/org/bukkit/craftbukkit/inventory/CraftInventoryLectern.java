package org.bukkit.craftbukkit.inventory;

import net.minecraft.inventory.Inventory;
import org.bukkit.block.Lectern;
import org.bukkit.inventory.LecternInventory;

import com.fungus_soft.bukkitfabric.interfaces.IMixinInventory;

public class CraftInventoryLectern extends CraftInventory implements LecternInventory {

    public CraftInventoryLectern(Inventory inventory) {
        super(inventory);
    }

    @Override
    public Lectern getHolder() {
        return (Lectern) ((IMixinInventory)(Object)inventory).getOwner();
    }

}