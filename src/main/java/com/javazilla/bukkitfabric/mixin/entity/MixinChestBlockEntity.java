package com.javazilla.bukkitfabric.mixin.entity;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.javazilla.bukkitfabric.interfaces.IMixinInventory;
import com.javazilla.bukkitfabric.interfaces.IMixinWorld;

import net.minecraft.block.Blocks;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;

@Mixin(ChestBlockEntity.class)
public class MixinChestBlockEntity implements IMixinInventory {

    @Shadow public DefaultedList<ItemStack> inventory;
    @Shadow public int viewerCount;

    public List<HumanEntity> transaction = new java.util.ArrayList<HumanEntity>();
    private int maxStack = MAX_STACK;

    @Override
    public List<ItemStack> getContents() {
        return inventory;
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
        BlockPos pos = ((ChestBlockEntity)(Object)this).pos;
        return new Location(((IMixinWorld)(((ChestBlockEntity)(Object)this).world)).getCraftWorld(), pos.x, pos.y, pos.z);
    }

    @Overwrite
    public void onOpen(PlayerEntity entityhuman) {
        if (!entityhuman.isSpectator()) {
            if (this.viewerCount < 0)
                this.viewerCount = 0;

            int oldPower = Math.max(0, Math.min(15, this.viewerCount)); // CraftBukkit - Get power before new viewer is added

            ++this.viewerCount;
            if (((ChestBlockEntity)(Object)this).world == null) return; // CraftBukkit

            // CraftBukkit start - Call redstone event
            if (((ChestBlockEntity)(Object)this).getCachedState().getBlock() == Blocks.TRAPPED_CHEST) {
                int newPower = Math.max(0, Math.min(15, this.viewerCount));
                if (oldPower != newPower)
                    org.bukkit.craftbukkit.event.CraftEventFactory.callRedstoneChange(((ChestBlockEntity)(Object)this).world, ((ChestBlockEntity)(Object)this).pos, oldPower, newPower);
            }
            // CraftBukkit end
            ((ChestBlockEntity)(Object)this).onInvOpenOrClose();
        }

    }

}