package org.cardboardpowered.impl.block;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.inventory.Inventory;
import org.cardboardpowered.impl.inventory.CardboardDoubleChestInventory;
import org.cardboardpowered.impl.world.WorldImpl;

import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.screen.NamedScreenHandlerFactory;

public class CardboardChest extends CardboardLootableBlock<ChestBlockEntity> implements Chest {

    public CardboardChest(final Block block) {
        super(block, ChestBlockEntity.class);
    }

    public CardboardChest(final Material material, final ChestBlockEntity te) {
        super(material, te);
    }

    @Override
    public Inventory getSnapshotInventory() {
        return new CraftInventory(this.getSnapshot());
    }

    @Override
    public Inventory getBlockInventory() {
        return (!this.isPlaced()) ? this.getSnapshotInventory() : new CraftInventory(this.getTileEntity());
    }

    @Override
    public Inventory getInventory() {
        CraftInventory inventory = (CraftInventory) this.getBlockInventory();
        if (!isPlaced()) return inventory;

        WorldImpl world = (WorldImpl) this.getWorld();

        ChestBlock blockChest = (ChestBlock) (this.getType() == Material.CHEST ? Blocks.CHEST : Blocks.TRAPPED_CHEST);
        NamedScreenHandlerFactory nms = blockChest.createScreenHandlerFactory(data, world.getHandle(), this.getPosition());

        return (nms instanceof DoubleInventory) ? (inventory = new CardboardDoubleChestInventory((DoubleInventory) nms)) : inventory;
    }

    @Override
    public void open() {
        // TODO Auto-generated method stub
    }

    @Override
    public void close() {
        // TODO Auto-generated method stub
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