package org.cardboardpowered.impl.block;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.ShulkerBox;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.inventory.Inventory;

import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;

public class CardboardShulkerBox extends CardboardLootableBlock<ShulkerBoxBlockEntity> implements ShulkerBox {

    public CardboardShulkerBox(final Block block) {
        super(block, ShulkerBoxBlockEntity.class);
    }

    public CardboardShulkerBox(final Material material, final ShulkerBoxBlockEntity te) {
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
    public DyeColor getColor() {
        net.minecraft.block.Block block = CraftMagicNumbers.getBlock(this.getType());
        return DyeColor.getByWoolData((byte) ((ShulkerBoxBlock) block).getColor().getId());
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