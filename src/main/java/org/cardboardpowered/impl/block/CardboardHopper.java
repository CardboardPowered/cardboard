package org.cardboardpowered.impl.block;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.inventory.Inventory;

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

}