package org.cardboardpowered.impl.inventory;

import net.minecraft.inventory.Inventory;
import org.bukkit.block.Lectern;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.inventory.LecternInventory;

import com.javazilla.bukkitfabric.interfaces.IMixinInventory;

public class CardboardLecternInventory extends CraftInventory implements LecternInventory {

    public CardboardLecternInventory(Inventory inventory) {
        super(inventory);
    }

    @Override
    public Lectern getHolder() {
        return (Lectern) ((IMixinInventory)(Object)inventory).getOwner();
    }

}