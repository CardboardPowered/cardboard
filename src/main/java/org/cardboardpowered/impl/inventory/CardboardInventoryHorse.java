package org.cardboardpowered.impl.inventory;

import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.ItemStack;
import net.minecraft.inventory.Inventory;

public class CardboardInventoryHorse extends CardboardInventoryAbstractHorse implements HorseInventory {

    public CardboardInventoryHorse(Inventory inventory) {
        super(inventory);
    }

    @Override
    public ItemStack getArmor() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setArmor(ItemStack arg0) {
        // TODO Auto-generated method stub
    }

}
