package com.javazilla.bukkitfabric.mixin.entity.block;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;
import org.spongepowered.asm.mixin.Mixin;

import com.javazilla.bukkitfabric.interfaces.IMixinInventory;
import com.javazilla.bukkitfabric.interfaces.IMixinWorld;

import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

@Mixin(BarrelBlockEntity.class)
public class MixinBarrelBlockEntity implements IMixinInventory {

    public List<HumanEntity> transaction = new ArrayList<>();
    private int maxStack = MAX_STACK;

    @Override
    public List<ItemStack> getContents() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InventoryHolder getOwner() {
        // TODO Auto-generated method stub
        return null;
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
    public Location getLocation() {
        BlockPos pos = ((BlockEntity)(Object)this).getPos();
        return new Location(((IMixinWorld)((BlockEntity)(Object)this).getWorld()).getCraftWorld(), pos.x, pos.y, pos.z);
    }

    @Override
    public int getMaxStackSize() {
        return maxStack;
    }

}
