package com.javazilla.bukkitfabric.mixin.entity.block;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.inventory.CraftInventoryDoubleChest;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.InventoryHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.javazilla.bukkitfabric.interfaces.IMixinEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinInventory;

import net.minecraft.block.entity.Hopper;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.vehicle.HopperMinecartEntity;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Direction;

@Mixin(HopperBlockEntity.class)
public class MixinHopperBlockEntity implements IMixinInventory {

    @Shadow
    public DefaultedList<ItemStack> inventory;

    public List<HumanEntity> transaction = new java.util.ArrayList<HumanEntity>();
    private int maxStack = MAX_STACK;

    public List<ItemStack> getContents() {
        return this.inventory;
    }

    public void onOpen(CraftHumanEntity who) {
        transaction.add(who);
    }

    public void onClose(CraftHumanEntity who) {
        transaction.remove(who);
    }

    public List<HumanEntity> getViewers() {
        return transaction;
    }

    @Override
    public int getMaxStackSize() {
        return maxStack;
    }

    public void setMaxStackSize(int size) {
        maxStack = size;
    }

    @Override public InventoryHolder getOwner() {return null;}
    @Override public Location getLocation() {return null;}

    @Inject(at = @At("HEAD"), method = "extract(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/entity/ItemEntity;)Z")
    private static void extract(net.minecraft.inventory.Inventory iinventory, ItemEntity entityitem, CallbackInfoReturnable<Boolean> ci) {
        if (iinventory instanceof IMixinInventory) {
            InventoryPickupItemEvent event = new InventoryPickupItemEvent(((IMixinInventory)iinventory).getOwner().getInventory(), (org.bukkit.entity.Item) ((IMixinEntity)entityitem).getBukkitEntity());
            Bukkit.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled())
                ci.setReturnValue(false);
        }
    }

    @Inject(at = @At("HEAD"), method = "extract(Lnet/minecraft/block/entity/Hopper;Lnet/minecraft/inventory/Inventory;ILnet/minecraft/util/math/Direction;)Z")
    private static void extract(Hopper ihopper, net.minecraft.inventory.Inventory iinventory, int i, Direction enumdirection, CallbackInfoReturnable<Boolean> ci) {
        ItemStack itemstack = iinventory.getStack(i);

        if (!itemstack.isEmpty() && canExtract(iinventory, itemstack, i, enumdirection)) {
            ItemStack itemstack1 = itemstack.copy();
            CraftItemStack oitemstack = CraftItemStack.asCraftMirror(iinventory.removeStack(i, 1));

            org.bukkit.inventory.Inventory sourceInventory;
            if (iinventory instanceof DoubleInventory) {
                sourceInventory = new CraftInventoryDoubleChest((DoubleInventory) iinventory);
            } else sourceInventory = ((IMixinInventory)iinventory).getOwner().getInventory();

            InventoryMoveItemEvent event = new InventoryMoveItemEvent(sourceInventory, oitemstack.clone(), ((IMixinInventory)ihopper).getOwner().getInventory(), false);
            Bukkit.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                iinventory.setStack(i, itemstack1);
                ci.setReturnValue(false);
            }
            int origCount = event.getItem().getAmount();
            ItemStack itemstack2 = transfer(iinventory, ihopper, CraftItemStack.asNMSCopy(event.getItem()), null);
            if (itemstack2.isEmpty()) {
                iinventory.markDirty();
                ci.setReturnValue(true);
            }
            itemstack1.decrement(origCount - itemstack2.getCount());
            iinventory.setStack(i, itemstack1);
        }
        ci.setReturnValue(false);
    }

    @Shadow
    public static ItemStack transfer(net.minecraft.inventory.Inventory iinventory, net.minecraft.inventory.Inventory iinventory1, ItemStack itemstack, Direction enumdirection) {
        return null;
    }

    @Shadow
    public static boolean canExtract(net.minecraft.inventory.Inventory iinventory, ItemStack itemstack, int i, Direction enumdirection) {
        return false;
    }

}