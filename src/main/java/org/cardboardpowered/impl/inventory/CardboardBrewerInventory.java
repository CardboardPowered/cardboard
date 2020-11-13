package org.cardboardpowered.impl.inventory;

import net.minecraft.inventory.Inventory;
import org.bukkit.block.BrewingStand;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;

import com.javazilla.bukkitfabric.interfaces.IMixinInventory;

public class CardboardBrewerInventory extends CraftInventory implements BrewerInventory {

    public CardboardBrewerInventory(Inventory inventory) {
        super(inventory);
    }

    @Override
    public ItemStack getIngredient() {
        return getItem(3);
    }

    @Override
    public void setIngredient(ItemStack ingredient) {
        setItem(3, ingredient);
    }

    @Override
    public BrewingStand getHolder() {
        return (BrewingStand) ((IMixinInventory)(Object)inventory).getOwner();
    }

    @Override
    public ItemStack getFuel() {
        return getItem(4);
    }

    @Override
    public void setFuel(ItemStack fuel) {
        setItem(4, fuel);
    }

}