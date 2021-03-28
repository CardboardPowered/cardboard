package com.javazilla.bukkitfabric.mixin.inventory;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.javazilla.bukkitfabric.interfaces.IMixinInventory;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

@Mixin(SimpleInventory.class)
public class MixinSimpleInventory implements IMixinInventory {

    @Shadow
    public DefaultedList<ItemStack> stacks;

    public List<HumanEntity> transaction = new java.util.ArrayList<HumanEntity>();
    public int maxStack_BF = MAX_STACK;

    @Override
    public List<ItemStack> getContents() {
        return stacks;
    }

    @Override
    public void onOpen(CraftHumanEntity who) {
        transaction.add(who);
    }

    @Override
    public void onClose(CraftHumanEntity who) {
        transaction.remove(who);
    }

    @Override
    public List<HumanEntity> getViewers() {
        return transaction;
    }

    @Override
    public InventoryHolder getOwner() {
        // TODO Auto-generated method stub
        if (transaction.size() >= 1)
            return transaction.get(0);
        return null;
    }

    @Override
    public void setMaxStackSize(int size) {
        maxStack_BF = size;
    }

    @Override
    public Location getLocation() {
        return null;
    }

    @Override
    public int getMaxStackSize() {
        return maxStack_BF;
    }

}