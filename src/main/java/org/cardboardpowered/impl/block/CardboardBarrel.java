package org.cardboardpowered.impl.block;

import net.minecraft.block.entity.BarrelBlockEntity;
import org.bukkit.Material;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.inventory.Inventory;

public class CardboardBarrel extends CardboardLootableBlock<BarrelBlockEntity> implements Barrel {

    public CardboardBarrel(Block block) {
        super(block, BarrelBlockEntity.class);
    }

    public CardboardBarrel(Material material, BarrelBlockEntity te) {
        super(material, te);
    }

    @Override
    public Inventory getSnapshotInventory() {
        return new CraftInventory(getSnapshot());
    }

    @Override
    public Inventory getInventory() {
        return (!this.isPlaced()) ? this.getSnapshotInventory() : new CraftInventory(getTileEntity());
    }

    @Override
    public void open() {
        // TODO Auto-generated method stub
    }

    @Override
    public void close() {
        // TODO Auto-generated method stub
    }

}
