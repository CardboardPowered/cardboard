package org.cardboardpowered.mixin.inventory;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.javazilla.bukkitfabric.interfaces.IMixinInventory;

import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

@Mixin(CraftingResultInventory.class)
public class MixinCraftingResultInventory implements IMixinInventory {

    @Shadow public DefaultedList<ItemStack> stacks;

    private int maxStack = MAX_STACK;

    @Override
    public List<ItemStack> getContents() {
        return stacks;
    }

    // MixinCraftingInventory takes care of this
    @Override public void onOpen(CraftHumanEntity who) {}
    @Override public void onClose(CraftHumanEntity who) {}
    @Override public List<HumanEntity> getViewers() {return new ArrayList<HumanEntity>();}

    @Override
    public InventoryHolder getOwner() {
        return null; // There is no owner for for the result inventory
    }

    @Override
    public void setMaxStackSize(int size) {
        maxStack = size;
    }

    @Override
    public Location getLocation() {
        return null; // No location for this inventory
    }

    @Override
    public int getMaxStackSize() {
        return maxStack;
    }

}