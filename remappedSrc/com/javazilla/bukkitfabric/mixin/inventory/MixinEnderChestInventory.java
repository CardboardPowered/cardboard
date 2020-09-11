package com.javazilla.bukkitfabric.mixin.inventory;

import org.bukkit.Location;
import org.bukkit.inventory.InventoryHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.javazilla.bukkitfabric.interfaces.IMixinWorld;

import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.inventory.EnderChestInventory;

@Mixin(EnderChestInventory.class)
public class MixinEnderChestInventory extends MixinSimpleInventory {

    @Shadow private EnderChestBlockEntity activeBlockEntity;

    public InventoryHolder getBukkitOwner() {
        return null; // TODO
    }

    @Override
    public Location getLocation() {
        return new Location(((IMixinWorld)this.activeBlockEntity.getWorld()).getCraftWorld(), this.activeBlockEntity.getPos().getX(), this.activeBlockEntity.getPos().getY(), this.activeBlockEntity.getPos().getZ());
    }

}