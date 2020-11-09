package org.cardboardpowered.impl.inventory;

import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import org.bukkit.block.Furnace;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;

import com.javazilla.bukkitfabric.interfaces.IMixinInventory;

public class CardboardFurnaceInventory extends CraftInventory implements FurnaceInventory {

    public CardboardFurnaceInventory(AbstractFurnaceBlockEntity inventory) {
        super(inventory);
    }

    @Override
    public ItemStack getResult() {
        return getItem(2);
    }

    @Override
    public ItemStack getFuel() {
        return getItem(1);
    }

    @Override
    public ItemStack getSmelting() {
        return getItem(0);
    }

    @Override
    public void setFuel(ItemStack stack) {
        setItem(1, stack);
    }

    @Override
    public void setResult(ItemStack stack) {
        setItem(2, stack);
    }

    @Override
    public void setSmelting(ItemStack stack) {
        setItem(0, stack);
    }

    @Override
    public Furnace getHolder() {
        return (Furnace) ((IMixinInventory)(Object)inventory).getOwner();
    }

}