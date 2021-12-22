package org.cardboardpowered.impl.inventory;

import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.inventory.AbstractHorseInventory;
import org.bukkit.inventory.ItemStack;
import net.minecraft.inventory.Inventory;

public class CardboardInventoryAbstractHorse extends CraftInventory implements AbstractHorseInventory {

    public CardboardInventoryAbstractHorse(Inventory inventory) {
        super(inventory);
    }

    @Override
    public ItemStack getSaddle() {
        return this.getItem(0);
    }

    @Override
    public void setSaddle(ItemStack stack) {
        this.setItem(0, stack);
    }

}
