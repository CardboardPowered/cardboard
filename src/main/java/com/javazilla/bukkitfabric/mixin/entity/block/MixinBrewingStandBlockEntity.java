package com.javazilla.bukkitfabric.mixin.entity.block;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.BrewingStandFuelEvent;
import org.bukkit.inventory.InventoryHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.javazilla.bukkitfabric.interfaces.IMixinInventory;
import com.javazilla.bukkitfabric.interfaces.IMixinWorld;

import net.minecraft.block.BlockState;
import net.minecraft.block.BrewingStandBlock;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.collection.DefaultedList;

@Mixin(BrewingStandBlockEntity.class)
public class MixinBrewingStandBlockEntity implements IMixinInventory {

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

    @Overwrite
    public void tick() {
        BrewingStandBlockEntity nms = (BrewingStandBlockEntity)(Object)this;
        ItemStack itemstack = (ItemStack) this.inventory.get(4);

        if (nms.fuel <= 0 && itemstack.getItem() == Items.BLAZE_POWDER) {
            // CraftBukkit start
            BrewingStandFuelEvent event = new BrewingStandFuelEvent(((IMixinWorld)nms.world).getCraftWorld().getBlockAt(nms.pos.getX(), nms.pos.getY(), nms.pos.getZ()), CraftItemStack.asCraftMirror(itemstack), 20);
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

    }

    @Override public InventoryHolder getOwner() {return null;}
    @Override public Location getLocation() {return null;}

}