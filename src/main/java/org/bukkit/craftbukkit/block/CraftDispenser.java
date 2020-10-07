package org.bukkit.craftbukkit.block;

import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.server.world.ServerWorld;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.projectiles.BlockProjectileSource;

import com.javazilla.bukkitfabric.impl.BlockProjectileSourceImpl;
import com.javazilla.bukkitfabric.impl.WorldImpl;

public class CraftDispenser extends CraftLootable<DispenserBlockEntity> implements Dispenser {

    public CraftDispenser(final Block block) {
        super(block, DispenserBlockEntity.class);
    }

    public CraftDispenser(final Material material, final DispenserBlockEntity te) {
        super(material, te);
    }

    @Override
    public Inventory getSnapshotInventory() {
        return new CraftInventory(this.getSnapshot());
    }

    @Override
    public Inventory getInventory() {
        return (!this.isPlaced()) ? this.getSnapshotInventory() : new CraftInventory(this.getTileEntity());
    }

    @Override
    public BlockProjectileSource getBlockProjectileSource() {
        if (getBlock().getType() != Material.DISPENSER) return null;
        return new BlockProjectileSourceImpl((DispenserBlockEntity) this.getTileEntityFromWorld());
    }

    @Override
    public boolean dispense() {
        Block block = getBlock();
        if (block.getType() == Material.DISPENSER) {
            ((DispenserBlock) Blocks.DISPENSER).dispense((ServerWorld)((WorldImpl) this.getWorld()).getHandle(), this.getPosition());
            return true;
        } else return false;
    }

}