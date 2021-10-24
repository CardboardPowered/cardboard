package org.cardboardpowered.mixin.entity.block;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.BrewingStandFuelEvent;
import org.bukkit.inventory.InventoryHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.interfaces.IMixinBrewingStandBlockEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinInventory;
import com.javazilla.bukkitfabric.interfaces.IMixinWorld;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(BrewingStandBlockEntity.class)
public class MixinBrewingStandBlockEntity implements IMixinInventory, IMixinBrewingStandBlockEntity {

    @Shadow
    public int fuel;

    @Shadow
    public DefaultedList<ItemStack> inventory;

    public List<HumanEntity> transaction = new java.util.ArrayList<HumanEntity>();
    private int maxStack = 64;

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
    private static void doBukkitEvent_BrewingStandFuelEvent(World world, BlockPos pos, BlockState state, BrewingStandBlockEntity be, CallbackInfo ci) {
        ItemStack itemstack = (ItemStack) ((IMixinBrewingStandBlockEntity)be).cardboard_getInventory().get(4);

        if (be.fuel <= 0 && itemstack.getItem() == Items.BLAZE_POWDER) {
            BrewingStandFuelEvent event = new BrewingStandFuelEvent(((IMixinWorld)be.world).getWorldImpl().getBlockAt(be.pos.getX(), be.pos.getY(), be.pos.getZ()), CraftItemStack.asCraftMirror(itemstack), 20);
            CraftServer.INSTANCE.getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                ci.cancel();
                return;
            }

            be.fuel = event.getFuelPower();
            if (be.fuel > 0 && event.isConsuming()) itemstack.decrement(1);
        }
    }

    // TODO 1.17ify:
   /* @Inject(at = @At("HEAD"), method = "craft", cancellable = true)
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
    }*/

    @Override public InventoryHolder getOwner() {return null;}
    @Override public Location getLocation() {return null;}

    @Override
    public DefaultedList<ItemStack> cardboard_getInventory() {
        return inventory;
    }

}