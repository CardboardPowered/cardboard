package com.javazilla.bukkitfabric.mixin.inventory;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.javazilla.bukkitfabric.impl.entity.AbstractVillagerImpl;
import com.javazilla.bukkitfabric.interfaces.IMixinEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinInventory;

import net.minecraft.entity.passive.AbstractTraderEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.village.Trader;
import net.minecraft.village.TraderInventory;

@Mixin(TraderInventory.class)
public class MixinTraderInventory implements IMixinInventory {

    @Shadow
    public DefaultedList<ItemStack> inventory;

    @Shadow
    public Trader trader;

    public List<HumanEntity> transaction = new java.util.ArrayList<HumanEntity>();
    private int maxStack = MAX_STACK;

    @Override
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
        trader.setCurrentCustomer((PlayerEntity) null);
    }

    @Override
    public List<HumanEntity> getViewers() {
        return transaction;
    }

    @Override
    public int getMaxStackSize() {
        return maxStack;
    }

    @Override
    public void setMaxStackSize(int i) {
        maxStack = i;
    }

    @Override
    public org.bukkit.inventory.InventoryHolder getOwner() {
        return (trader instanceof AbstractTraderEntity) ? (AbstractVillagerImpl) ((IMixinEntity)((AbstractTraderEntity) this.trader)).getBukkitEntity() : null;
    }

    @Override
    public Location getLocation() {
        return (trader instanceof VillagerEntity) ? ((IMixinEntity)((VillagerEntity) this.trader)).getBukkitEntity().getLocation() : null;
    }

}