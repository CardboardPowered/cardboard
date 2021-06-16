package org.cardboardpowered.impl.block;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Nullable;

import net.kyori.adventure.text.Component;
import net.minecraft.block.entity.HopperBlockEntity;

public class CardboardHopper extends CardboardLootableBlock<HopperBlockEntity> implements Hopper {

    public CardboardHopper(final Block block) {
        super(block, HopperBlockEntity.class);
    }

    public CardboardHopper(final Material material, final HopperBlockEntity te) {
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

    @Override
    public @Nullable Component customName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void customName(@Nullable Component arg0) {
        // TODO Auto-generated method stub
        
    }

}