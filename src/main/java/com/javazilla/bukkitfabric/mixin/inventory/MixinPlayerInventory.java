package com.javazilla.bukkitfabric.mixin.inventory;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;
import org.spongepowered.asm.mixin.Mixin;

import com.javazilla.bukkitfabric.interfaces.IMixinInventory;
import com.javazilla.bukkitfabric.interfaces.IMixinPlayerInventory;
import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

@Mixin(PlayerInventory.class)
public class MixinPlayerInventory implements IMixinInventory, IMixinPlayerInventory {

    private PlayerInventory get() {
        return (PlayerInventory) (Object) this;
    }

    @Override
    public List<ItemStack> getContents() {
        // TODO Auto-generated method stub
        return get().main;
    }

    @Override
    public void onOpen(CraftHumanEntity who) {
        get().onOpen((PlayerEntity) who.nms);
    }

    @Override
    public void onClose(CraftHumanEntity who) {
        get().onClose((PlayerEntity) who.nms);
    }

    @Override
    public List<HumanEntity> getViewers() {
        // TODO Auto-generated method stub
        return Arrays.asList(((CraftPlayer)((IMixinServerEntityPlayer)get().player).getBukkitEntity()));
    }

    @Override
    public InventoryHolder getOwner() {
        return ((CraftPlayer)((IMixinServerEntityPlayer)get().player).getBukkitEntity());
    }

    @Override
    public void setMaxStackSize(int size) {
    }

    @Override
    public Location getLocation() {
        return ((CraftPlayer)((IMixinServerEntityPlayer)get().player).getBukkitEntity()).getLocation();
    }

    @Override
    public int getMaxStackSize() {
        return get().getMaxCountPerStack();
    }

    @Override
    public int canHold(ItemStack itemstack) {
        int remains = itemstack.getCount();
        for (int i = 0; i < get().main.size(); ++i) {
            ItemStack itemstack1 = get().getStack(i);
            if (itemstack1.isEmpty()) return itemstack.getCount();

            if (get().canStackAddMore(itemstack1, itemstack))
                remains -= (itemstack1.getMaxCount() < getMaxStackSize() ? itemstack1.getMaxCount() : getMaxStackSize()) - itemstack1.getCount();
            if (remains <= 0) return itemstack.getCount();
        }
        ItemStack offhandItemStack = get().getStack(get().main.size() + get().armor.size());
        if (get().canStackAddMore(offhandItemStack, itemstack))
            remains -= (offhandItemStack.getMaxCount() < get().getMaxCountPerStack() ? offhandItemStack.getMaxCount() : get().getMaxCountPerStack()) - offhandItemStack.getCount();
        if (remains <= 0) return itemstack.getCount();

        return itemstack.getCount() - remains;
    }

}