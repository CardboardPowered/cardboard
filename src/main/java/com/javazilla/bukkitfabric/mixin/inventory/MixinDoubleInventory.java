package com.javazilla.bukkitfabric.mixin.inventory;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.cardboardpowered.impl.entity.HumanEntityImpl;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.javazilla.bukkitfabric.interfaces.IMixinInventory;

import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

@Mixin(DoubleInventory.class)
public class MixinDoubleInventory implements IMixinInventory {

    @Shadow public Inventory first;
    @Shadow public Inventory second;

    public List<HumanEntity> transaction = new java.util.ArrayList<HumanEntity>();

    @Override
    public List<ItemStack> getContents() {
        List<ItemStack> result = new ArrayList<ItemStack>(this.first.size() + this.second.size());
        for (int i = 0; i < (this.first.size() + this.second.size()); i++)
            result.add(this.getStack(i));
        return result;
    }

    @Shadow
    public ItemStack getStack(int i) {
        return null;
    }

    @Override
    public void onOpen(HumanEntityImpl who) {
        this.first.onOpen(who.getHandle());
        this.second.onOpen(who.getHandle());
        transaction.add(who);
    }

    @Override
    public void onClose(HumanEntityImpl who) {
        this.first.onClose(who.getHandle());
        this.second.onClose(who.getHandle());
        transaction.remove(who);
    }

    @Override
    public List<HumanEntity> getViewers() {
        return transaction;
    }

    @Override
    public InventoryHolder getOwner() {
        return null; // Bukkit DoubleChest does not refer to this method.
    }

    @Override
    public void setMaxStackSize(int size) {
        ((IMixinInventory)this.first).setMaxStackSize(size);
        ((IMixinInventory)this.second).setMaxStackSize(size);
    }

    @Override
    public Location getLocation() {
        return ((IMixinInventory)this.first).getLocation();
    }

    @Override
    public int getMaxStackSize() {
        return MAX_STACK;
    }

}