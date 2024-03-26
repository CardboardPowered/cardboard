package org.cardboardpowered.impl.inventory;

import net.minecraft.inventory.Inventory;
import org.bukkit.block.Jukebox;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.JukeboxInventory;

import com.javazilla.bukkitfabric.interfaces.IMixinInventory;

public class CraftInventoryJukebox
extends CraftInventory
implements JukeboxInventory {
    public CraftInventoryJukebox(Inventory inventory) {
        super(inventory);
    }

    public void setRecord(ItemStack item) {
        if (item == null) {
            this.inventory.removeStack(0, 0);
        } else {
            this.setItem(0, item);
        }
    }

    public ItemStack getRecord() {
        return this.getItem(0);
    }

    public Jukebox getHolder() {
        return (Jukebox) ((IMixinInventory)this.inventory).getOwner();
    }
}

