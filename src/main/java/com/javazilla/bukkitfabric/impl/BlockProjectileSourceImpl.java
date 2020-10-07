package com.javazilla.bukkitfabric.impl;

import net.minecraft.block.entity.DispenserBlockEntity;
import org.bukkit.block.Block;
import org.bukkit.entity.Projectile;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.util.Vector;

import com.javazilla.bukkitfabric.interfaces.IMixinWorld;

public class BlockProjectileSourceImpl implements BlockProjectileSource {

    private final DispenserBlockEntity dispenserBlock;

    public BlockProjectileSourceImpl(DispenserBlockEntity dispenserBlock) {
        this.dispenserBlock = dispenserBlock;
    }

    @Override
    public Block getBlock() {
        return ((IMixinWorld)(Object)dispenserBlock.getWorld()).getWorldImpl().getBlockAt(dispenserBlock.getPos().getX(), dispenserBlock.getPos().getY(), dispenserBlock.getPos().getZ());
    }

    @Override
    public <T extends Projectile> T launchProjectile(Class<? extends T> projectile) {
        return launchProjectile(projectile, null);
    }

    @Override
    public <T extends Projectile> T launchProjectile(Class<? extends T> projectile, Vector velocity) {
        // TODO Bukkit4Fabric: Auto-generated method stub
        return null;
    }

}