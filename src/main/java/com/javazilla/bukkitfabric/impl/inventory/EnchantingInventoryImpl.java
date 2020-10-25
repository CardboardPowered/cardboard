package com.javazilla.bukkitfabric.impl.inventory;

import net.minecraft.inventory.Inventory;

import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.inventory.EnchantingInventory;
import org.bukkit.inventory.ItemStack;

public class EnchantingInventoryImpl extends CraftInventory implements EnchantingInventory {

    public EnchantingInventoryImpl(Inventory inventory) {
        super(inventory);
    }

    @Override
    public void setItem(ItemStack item) {
        setItem(0, item);
    }

    @Override
    public ItemStack getItem() {
        return getItem(0);
    }

    @Override
    public void setSecondary(ItemStack item) {
        setItem(1, item);
    }

    @Override
    public ItemStack getSecondary() {
        return getItem(1);
    }

}