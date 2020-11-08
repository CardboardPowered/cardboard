package com.javazilla.bukkitfabric.mixin.entity.block;

import java.util.List;

import org.bukkit.Location;
import org.cardboardpowered.impl.entity.HumanEntityImpl;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.javazilla.bukkitfabric.interfaces.IMixinInventory;

import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

@Mixin(ShulkerBoxBlockEntity.class)
public class MixinShulkerBoxBlockEntity implements IMixinInventory {

    @Shadow
    public DefaultedList<ItemStack> inventory;

    public List<HumanEntity> transaction = new java.util.ArrayList<HumanEntity>();
    private int maxStack = MAX_STACK;

    @Override
    public List<ItemStack> getContents() {
        return inventory;
    }

    @Override
    public void onOpen(HumanEntityImpl who) {
        transaction.add(who);
    }

    @Override
    public void onClose(HumanEntityImpl who) {
        transaction.remove(who);
    }

    @Override
    public List<HumanEntity> getViewers() {
        return transaction;
    }

    @Override
    public void setMaxStackSize(int size) {
        this.maxStack = size;
    }

    @Override
    public int getMaxStackSize() {
        return maxStack;
    }

    @Override public Location getLocation(){return null;}
    @Override public InventoryHolder getOwner(){return null;}

}
