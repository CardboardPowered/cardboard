package org.cardboardpowered.impl.inventory;

import net.minecraft.inventory.Inventory;

import org.bukkit.craftbukkit.inventory.CraftResultInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.SmithingInventory;
import org.jetbrains.annotations.Nullable;

public class CardboardSmithingInventory extends CraftResultInventory implements SmithingInventory {

    public CardboardSmithingInventory(Inventory inventory, Inventory resultInventory) {
        super(inventory, resultInventory);
    }

    @Override
    public int close() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public @Nullable Recipe getRecipe() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public @Nullable ItemStack getResult() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setResult(@Nullable ItemStack arg0) {
        // TODO Auto-generated method stub
        
    }

}