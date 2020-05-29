package com.fungus_soft.bukkitfabric.mixin;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;
import org.spongepowered.asm.mixin.Mixin;
import com.fungus_soft.bukkitfabric.interfaces.IMixinInventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

@Mixin(Inventory.class)
public class InventoryMixin implements IMixinInventory {

    private Inventory get() {
        return (Inventory) (Object) this;
    }

    @Override
    public List<ItemStack> getContents() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onOpen(CraftHumanEntity who) {
        get().onInvOpen((PlayerEntity) who.nms);
    }

    @Override
    public void onClose(CraftHumanEntity who) {
        get().onInvClose((PlayerEntity) who.nms);
    }

    @Override
    public List<HumanEntity> getViewers() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InventoryHolder getOwner() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setMaxStackSize(int size) {
        // TODO Auto-generated method stub
    }

    @Override
    public Location getLocation() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getMaxStackSize() {
        return get().getInvMaxStackAmount();
    }

}