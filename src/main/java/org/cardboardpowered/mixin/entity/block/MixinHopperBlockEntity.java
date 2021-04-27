package org.cardboardpowered.mixin.entity.block;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.InventoryHolder;
import org.cardboardpowered.impl.inventory.CardboardDoubleChestInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.javazilla.bukkitfabric.interfaces.IMixinEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinInventory;
import com.javazilla.bukkitfabric.interfaces.IMixinWorld;

import net.minecraft.block.entity.Hopper;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.DoubleInventory;
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

    @Override 
    public InventoryHolder getOwner() {
        HopperBlockEntity b = (HopperBlockEntity) (Object)this;
        if (b.world == null) return null;
        org.bukkit.block.Block block = ((IMixinWorld)b.world).getWorldImpl().getBlockAt(b.pos.getX(), b.pos.getY(), b.pos.getZ());
        if (block == null) {
            org.bukkit.Bukkit.getLogger().log(java.util.logging.Level.WARNING, "No block for owner at %s %d %d %d", new Object[]{b.world, b.pos.getX(), b.pos.getY(), b.pos.getZ()});
            return null;
        }
        org.bukkit.block.BlockState state = block.getState();
        return (state instanceof InventoryHolder) ? (InventoryHolder) state : null;
    }

    @Override
    public Location getLocation() {
        HopperBlockEntity b = (HopperBlockEntity) (Object)this;
        return new Location(((IMixinWorld)b.world).getWorldImpl(), b.pos.getX(), b.pos.getY(), b.pos.getZ());
    }

    @Inject(at = @At("HEAD"), method = "extract(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/entity/ItemEntity;)Z", cancellable = true)
    private static void extract1(net.minecraft.inventory.Inventory iinventory, ItemEntity entityitem, CallbackInfoReturnable<Boolean> ci) {
        try {
            if (iinventory instanceof IMixinInventory) {
                InventoryPickupItemEvent event = new InventoryPickupItemEvent(((IMixinInventory)iinventory).getOwner().getInventory(),(org.bukkit.entity.Item) ((IMixinEntity)entityitem).getBukkitEntity());
                Bukkit.getServer().getPluginManager().callEvent(event);
                if (event.isCancelled())
                    ci.setReturnValue(false);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Inject(at = @At("HEAD"), method = "extract(Lnet/minecraft/block/entity/Hopper;Lnet/minecraft/inventory/Inventory;ILnet/minecraft/util/math/Direction;)Z", cancellable = true)
    private static void extract2(Hopper ihopper, net.minecraft.inventory.Inventory iinventory, int i, Direction enumdirection, CallbackInfoReturnable<Boolean> ci) {
        ItemStack itemstack = iinventory.getStack(i);
        boolean error = false;

        try {
            if (!itemstack.isEmpty() && canExtract(iinventory, itemstack, i, enumdirection)) {
                ItemStack itemstack1 = itemstack.copy();
                if (iinventory instanceof IMixinInventory && ihopper instanceof IMixinInventory) {
                    CraftItemStack oitemstack = CraftItemStack.asCraftMirror(iinventory.removeStack(i, 1));
                    org.bukkit.inventory.Inventory sourceInventory;
                    if (iinventory instanceof DoubleInventory) {
                        sourceInventory = new CardboardDoubleChestInventory((DoubleInventory) iinventory);
                    } else sourceInventory = ((IMixinInventory)iinventory).getOwner().getInventory();
        
                    InventoryMoveItemEvent event = new InventoryMoveItemEvent(sourceInventory, oitemstack.clone(), ((IMixinInventory)ihopper).getOwner().getInventory(), false);
                    Bukkit.getServer().getPluginManager().callEvent(event);
                    if (event.isCancelled()) {
                        iinventory.setStack(i, itemstack1);
                        // TODO: Somehow this breaks Mixin?
                       // if (ihopper instanceof HopperBlockEntity) {
                        //  ((HopperBlockEntity) ihopper).setCooldown(8); // Delay hopper checks
                       // } //else if (ihopper instanceof HopperMinecartEntity) {
                         //   ((HopperMinecartEntity) ihopper).setTransferCooldown(4); // Delay hopper minecart checks
                        //}
                        ci.setReturnValue(false);
                        return;
                    }
                  //  int origCount = event.getItem().getAmount();
                    ItemStack itemstack2 = transfer(iinventory, ihopper, CraftItemStack.asNMSCopy(event.getItem()), null);
                    if (itemstack2.isEmpty()) {
                        iinventory.markDirty();
                        ci.setReturnValue(true);
                        return;
                    }
                   // itemstack1.decrement(origCount - itemstack2.getCount());
                    iinventory.setStack(i, itemstack1);
                } else {
                    error = true;
                }
            }
            if (!error) ci.setReturnValue(false);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
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