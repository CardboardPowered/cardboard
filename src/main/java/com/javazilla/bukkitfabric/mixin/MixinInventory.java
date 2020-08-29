package com.javazilla.bukkitfabric.mixin;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.javazilla.bukkitfabric.interfaces.IMixinInventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

@Mixin(SimpleInventory.class)
public class MixinInventory implements IMixinInventory {

    @Shadow
    @Final
    public DefaultedList<ItemStack> stacks;

    public List<HumanEntity> transaction = new java.util.ArrayList<HumanEntity>();

    private SimpleInventory get() {
        return (SimpleInventory) (Object) this;
    }

    @Override
    public List<ItemStack> getContents() {
        return stacks;
    }

    @Override
    public void onOpen(CraftHumanEntity who) {
        transaction.add(who);
        get().onOpen((PlayerEntity) who.nms);
    }

    @Override
    public void onClose(CraftHumanEntity who) {
        transaction.remove(who);
        get().onClose((PlayerEntity) who.nms);
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
        // TODO Auto-generated method stub
    }

    @Override
    public Location getLocation() {
        return null;
    }

    @Override
    public int getMaxStackSize() {
        return get().getMaxCountPerStack();
    }

}