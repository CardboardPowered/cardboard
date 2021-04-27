package org.cardboardpowered.mixin.inventory;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.javazilla.bukkitfabric.interfaces.IMixinInventory;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.collection.DefaultedList;

@Mixin(CraftingInventory.class)
public class MixinCraftingInventory implements IMixinInventory {

    @Shadow public ScreenHandler handler;
    @Shadow public DefaultedList<ItemStack> stacks;

    public List<HumanEntity> transaction = new java.util.ArrayList<HumanEntity>();
    public int maxStack = MAX_STACK;

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
        return null;
    }

    @Override
    public void setMaxStackSize(int size) {
        maxStack = size;
    }

    @Override
    public Location getLocation() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getMaxStackSize() {
        return maxStack;
    }

}