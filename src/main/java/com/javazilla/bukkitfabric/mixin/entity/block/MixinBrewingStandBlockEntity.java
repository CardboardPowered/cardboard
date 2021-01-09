package com.javazilla.bukkitfabric.mixin.entity.block;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.cardboardpowered.impl.entity.HumanEntityImpl;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.BrewingStandFuelEvent;
import org.bukkit.inventory.InventoryHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.interfaces.IMixinInventory;
import com.javazilla.bukkitfabric.interfaces.IMixinWorld;

import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;

@Mixin(BrewingStandBlockEntity.class)
public class MixinBrewingStandBlockEntity implements IMixinInventory {

    @Shadow
    public int fuel;

    @Shadow
    public DefaultedList<ItemStack> inventory;

    public List<HumanEntity> transaction = new java.util.ArrayList<HumanEntity>();
    private int maxStack = 64;

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
    public List<ItemStack> getContents() {
        return this.inventory;
    }

    @Override
    public int getMaxStackSize() {
        return maxStack;
    }

    @Override
    public void setMaxStackSize(int size) {
        maxStack = size;
    }

    /**
     * @author CardboardPowered.org
     * @reason BrewingStandFuelEvent
     */
    @Inject(at = @At("HEAD"), method = "tick", cancellable = true)
    public void doBukkitEvent_BrewingStandFuelEvent(CallbackInfo ci) {
        BrewingStandBlockEntity nms = (BrewingStandBlockEntity)(Object)this;
        ItemStack itemstack = (ItemStack) this.inventory.get(4);

        if (nms.fuel <= 0 && itemstack.getItem() == Items.BLAZE_POWDER) {
            BrewingStandFuelEvent event = new BrewingStandFuelEvent(((IMixinWorld)nms.world).getWorldImpl().getBlockAt(nms.pos.getX(), nms.pos.getY(), nms.pos.getZ()), CraftItemStack.asCraftMirror(itemstack), 20);
            CraftServer.INSTANCE.getPluginManager().callEvent(event);
    
            if (event.isCancelled()) {
                ci.cancel();
                return;
            }
    
            nms.fuel = event.getFuelPower();
            if (nms.fuel > 0 && event.isConsuming()) itemstack.decrement(1);
        }
    }

    @Inject(at = @At("HEAD"), method = "craft", cancellable = true)
    public void doBukkitEvent_BrewEvent(CallbackInfo ci) {
        InventoryHolder owner = this.getOwner();
        if (owner != null) {
            BlockPos pos = ((BrewingStandBlockEntity)(Object)this).getPos();
            BrewEvent event = new BrewEvent(((IMixinWorld)((BrewingStandBlockEntity)(Object)this).getWorld()).getWorldImpl().getBlockAt(pos.getX(), pos.getY(), pos.getZ()), (org.bukkit.inventory.BrewerInventory) owner.getInventory(), this.fuel);
            org.bukkit.Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                ci.cancel();
                return;
            }
        }
    }

    @Override public InventoryHolder getOwner() {return null;}
    @Override public Location getLocation() {return null;}

}