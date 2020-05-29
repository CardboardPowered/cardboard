package com.fungus_soft.bukkitfabric.interfaces;

import org.bukkit.craftbukkit.entity.CraftHumanEntity;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;

public interface IMixinInventory {

    java.util.List<ItemStack> getContents();

    void onOpen(CraftHumanEntity who);

    void onClose(CraftHumanEntity who);

    java.util.List<org.bukkit.entity.HumanEntity> getViewers();

    org.bukkit.inventory.InventoryHolder getOwner();

    void setMaxStackSize(int size);

    org.bukkit.Location getLocation();

    default Recipe getCurrentRecipe() {
        return null;
    }

    default void setCurrentRecipe(Recipe recipe) {
    }

    int MAX_STACK = 64;

    int getMaxStackSize();

}