package com.javazilla.bukkitfabric.impl.entity;

import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.ThrowableProjectile;
import org.bukkit.inventory.ItemStack;

import com.javazilla.bukkitfabric.interfaces.IMixinThrownItemEntity;

public abstract class ThrowableProjectileImpl extends ProjectileImpl implements ThrowableProjectile {

    public ThrowableProjectileImpl(CraftServer server, ProjectileEntity entity) {
        super(server, entity);
    }

    @Override
    public ItemStack getItem() {
        if (((IMixinThrownItemEntity)getHandle()).getItemBF().isEmpty()) {
            return CraftItemStack.asBukkitCopy(new net.minecraft.item.ItemStack(((IMixinThrownItemEntity)getHandle()).getDefaultItemPublic()));
        } else return CraftItemStack.asBukkitCopy(((IMixinThrownItemEntity)getHandle()).getItemBF());
    }

    @Override
    public void setItem(ItemStack item) {
        getHandle().setItem(CraftItemStack.asNMSCopy(item));
    }

    @Override
    public ThrownItemEntity getHandle() {
        return (ThrownItemEntity) nms;
    }

}