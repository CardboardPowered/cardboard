package com.javazilla.bukkitfabric.mixin.entity.block;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
        return new Location(((IMixinWorld)(((ChestBlockEntity)(Object)this).world)).getWorldImpl(), pos.x, pos.y, pos.z);
    }

    private int oldPower_B;

    /**
     * @reason Redstone Event - store old power value
     */
    @Inject(at = @At("HEAD"), method = "onOpen")
    public void doBukkitEvent_RedstoneChange_1(PlayerEntity e, CallbackInfo ci) {
        oldPower_B = Math.max(0, Math.min(15, this.viewerCount)); // CraftBukkit - Get power before new viewer is added
    }

    /**
     * @reason Redstone Event
     */
    @Inject(at = @At("TAIL"), method = "onOpen")
    public void doBukkitEvent_RedstoneChange_2(PlayerEntity e, CallbackInfo ci) {
        if (((ChestBlockEntity)(Object)this).getCachedState().getBlock() == Blocks.TRAPPED_CHEST) {
            int newPower = Math.max(0, Math.min(15, this.viewerCount));
            if (oldPower_B != newPower)
                BukkitEventFactory.callRedstoneChange(((ChestBlockEntity)(Object)this).world, ((ChestBlockEntity)(Object)this).pos, oldPower_B, newPower);
        }
    }

}