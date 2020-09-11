package com.javazilla.bukkitfabric.mixin.entity.block;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.javazilla.bukkitfabric.interfaces.IMixinInventory;

import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

@Mixin(DispenserBlockEntity.class)
public class MixinDispenserBlockEntity implements IMixinInventory {

    @Shadow
    public DefaultedList<ItemStack> inventory;

    public List<HumanEntity> transaction = new java.util.ArrayList<HumanEntity>();
    private int maxStack = MAX_STACK;

    public List<ItemStack> getContents() {
        return this.inventory;
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
    public void setMaxStackSize(int size) {
        maxStack = size;
    }

    @Override
    public int getMaxStackSize() {
        return maxStack;
    }

    @Override
    public InventoryHolder getOwner() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Location getLocation() {
        // TODO Auto-generated method stub
        return null;
    }

}