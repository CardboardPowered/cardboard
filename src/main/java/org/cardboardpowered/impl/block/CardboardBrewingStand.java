package org.cardboardpowered.impl.block;

import net.minecraft.block.entity.BrewingStandBlockEntity;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BrewingStand;
import org.bukkit.craftbukkit.block.CraftContainer;
import org.bukkit.inventory.BrewerInventory;
import org.cardboardpowered.impl.inventory.CardboardBrewerInventory;

public class CardboardBrewingStand extends CraftContainer<BrewingStandBlockEntity> implements BrewingStand {

    public CardboardBrewingStand(Block block) {
        super(block, BrewingStandBlockEntity.class);
    }

    public CardboardBrewingStand(final Material material, final BrewingStandBlockEntity te) {
        super(material, te);
    }

    @Override
    public BrewerInventory getSnapshotInventory() {
        return new CardboardBrewerInventory(this.getSnapshot());
    }

    @Override
    public BrewerInventory getInventory() {
        return (!this.isPlaced()) ? this.getSnapshotInventory() : new CardboardBrewerInventory(this.getTileEntity());
    }

    @Override
    public int getBrewingTime() {
        return this.getSnapshot().brewTime;
    }

    @Override
    public void setBrewingTime(int brewTime) {
        this.getSnapshot().brewTime = brewTime;
    }

    @Override
    public int getFuelLevel() {
        return this.getSnapshot().fuel;
    }

    @Override
    public void setFuelLevel(int level) {
        this.getSnapshot().fuel = level;
    }

}