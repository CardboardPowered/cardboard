package org.cardboardpowered.impl.block;

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

}