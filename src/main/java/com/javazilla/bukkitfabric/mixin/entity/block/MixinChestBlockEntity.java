package com.javazilla.bukkitfabric.mixin.entity.block;

import java.util.List;

import org.bukkit.Location;
import org.cardboardpowered.impl.entity.HumanEntityImpl;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;
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
    // TODO 1.17 @Shadow public int viewerCount;

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
        return new Location(((IMixinWorld)(((ChestBlockEntity)(Object)this).world)).getWorldImpl(), pos.x, pos.y, pos.z);
    }

    /**
     * @author BukkitFabric
     * @reason Redstone Events
     */
    // TODO 1.17
    /*@Overwrite
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
                    BukkitEventFactory.callRedstoneChange(((ChestBlockEntity)(Object)this).world, ((ChestBlockEntity)(Object)this).pos, oldPower, newPower);
            }
            // CraftBukkit end
            ((ChestBlockEntity)(Object)this).onInvOpenOrClose();
        }

    }*/

}