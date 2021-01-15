package org.cardboardpowered.impl.block;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Dropper;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.inventory.Inventory;
import org.cardboardpowered.impl.world.WorldImpl;

import net.minecraft.block.Blocks;
import net.minecraft.block.DropperBlock;
import net.minecraft.block.entity.DropperBlockEntity;
import net.minecraft.server.world.ServerWorld;

public class CardboardDropper extends CardboardLootableBlock<DropperBlockEntity> implements Dropper {

    public CardboardDropper(final Block block) {
        super(block, DropperBlockEntity.class);
    }

    public CardboardDropper(final Material material, DropperBlockEntity te) {
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
    public void drop() {
        Block block = getBlock();

        if (block.getType() == Material.DROPPER) {
            WorldImpl world = (WorldImpl) this.getWorld();
            DropperBlock drop = (DropperBlock) Blocks.DROPPER;
            drop.dispense((ServerWorld) world.getHandle(), this.getPosition());
        }
    }

    @Override
    public long getLastFilled() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Long getLastLooted(UUID arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getNextRefill() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean hasBeenFilled() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean hasPendingRefill() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean hasPlayerLooted(UUID arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isRefillEnabled() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean setHasPlayerLooted(UUID arg0, boolean arg1) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public long setNextRefill(long arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

}