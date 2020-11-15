package com.javazilla.bukkitfabric.mixin.entity.block;

import java.util.Arrays;
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
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.interfaces.IMixinInventory;
import com.javazilla.bukkitfabric.interfaces.IMixinWorld;

import net.minecraft.block.BlockState;
import net.minecraft.block.BrewingStandBlock;
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
     * @author BukkitFabric
     * @reason BrewingStandFuelEvent
     */
    // TODO 1.17
    /*@Overwrite
    public void tick() {
        BrewingStandBlockEntity nms = (BrewingStandBlockEntity)(Object)this;
        ItemStack itemstack = (ItemStack) this.inventory.get(4);

        if (nms.fuel <= 0 && itemstack.getItem() == Items.BLAZE_POWDER) {
            // CraftBukkit start
            BrewingStandFuelEvent event = new BrewingStandFuelEvent(((IMixinWorld)nms.world).getWorldImpl().getBlockAt(nms.pos.getX(), nms.pos.getY(), nms.pos.getZ()), CraftItemStack.asCraftMirror(itemstack), 20);
            CraftServer.INSTANCE.getPluginManager().callEvent(event);

            if (event.isCancelled())
                return;

            nms.fuel = event.getFuelPower();
            if (nms.fuel > 0 && event.isConsuming())
                itemstack.decrement(1);
            // CraftBukkit end
            nms.markDirty();
        }

        boolean flag = nms.canCraft();
        boolean flag1 = nms.brewTime > 0;
        ItemStack itemstack1 = (ItemStack) this.inventory.get(3);

        if (flag1) {
            --nms.brewTime;
            boolean flag2 = nms.brewTime == 0;

            if (flag2 && flag) {
                nms.craft();
                nms.markDirty();
            } else if (!flag) {
                nms.brewTime = 0;
                nms.markDirty();
            } else if (nms.itemBrewing != itemstack1.getItem()) {
                nms.brewTime = 0;
                nms.markDirty();
            }
        } else if (flag && nms.fuel > 0) {
            --nms.fuel;
            nms.brewTime = 400;
            nms.itemBrewing = itemstack1.getItem();
            nms.markDirty();
        }

        if (!nms.world.isClient) {
            boolean[] aboolean = nms.getSlotsEmpty();

            if (!Arrays.equals(aboolean, nms.slotsEmptyLastTick)) {
                nms.slotsEmptyLastTick = aboolean;
                BlockState iblockdata = nms.world.getBlockState(nms.getPos());

                if (!(iblockdata.getBlock() instanceof BrewingStandBlock))
                    return;

                for (int i = 0; i < BrewingStandBlock.BOTTLE_PROPERTIES.length; ++i)
                    iblockdata = (BlockState) iblockdata.with(BrewingStandBlock.BOTTLE_PROPERTIES[i], aboolean[i]);

                nms.world.setBlockState(nms.pos, iblockdata, 2);
            }
        }

    }*/

    // TODO 1.17
    /*@Inject(at = @At("HEAD"), method = "craft", cancellable = true)
    public void doCraft(CallbackInfo ci) {
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

}