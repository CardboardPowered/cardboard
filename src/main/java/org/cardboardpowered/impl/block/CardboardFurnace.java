package org.cardboardpowered.impl.block;

import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.craftbukkit.block.CraftContainer;
import org.bukkit.inventory.FurnaceInventory;
import org.cardboardpowered.impl.inventory.CardboardFurnaceInventory;

public class CardboardFurnace<T extends AbstractFurnaceBlockEntity> extends CraftContainer<T> implements Furnace {

    public CardboardFurnace(Block block, Class<T> tileEntityClass) {
        super(block, tileEntityClass);
    }

    public CardboardFurnace(final Material material, final T te) {
        super(material, te);
    }

    @Override
    public FurnaceInventory getSnapshotInventory() {
        return new CardboardFurnaceInventory(this.getSnapshot());
    }

    @Override
    public FurnaceInventory getInventory() {
        return (!this.isPlaced()) ? this.getSnapshotInventory() : new CardboardFurnaceInventory(this.getTileEntity());
    }

    @Override
    public short getBurnTime() {
        return (short) this.getSnapshot().burnTime;
    }

    @Override
    public void setBurnTime(short burnTime) {
        this.getSnapshot().burnTime = burnTime;
        this.data = this.data.with(AbstractFurnaceBlock.LIT, burnTime > 0);
    }

    @Override
    public short getCookTime() {
        return (short) this.getSnapshot().cookTime;
    }

    @Override
    public void setCookTime(short cookTime) {
        this.getSnapshot().cookTime = cookTime;
    }

    @Override
    public int getCookTimeTotal() {
        return this.getSnapshot().cookTimeTotal;
    }

    @Override
    public void setCookTimeTotal(int cookTimeTotal) {
        this.getSnapshot().cookTimeTotal = cookTimeTotal;
    }

}