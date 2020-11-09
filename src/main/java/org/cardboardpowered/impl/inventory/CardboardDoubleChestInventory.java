package org.cardboardpowered.impl.inventory;

import org.bukkit.Location;
import org.bukkit.block.DoubleChest;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.javazilla.bukkitfabric.impl.ChestBlockDoubleInventory;

import net.minecraft.inventory.DoubleInventory;
import net.minecraft.screen.NamedScreenHandlerFactory;

public class CardboardDoubleChestInventory extends CraftInventory implements DoubleChestInventory {

    public NamedScreenHandlerFactory tile;
    private final CraftInventory left;
    private final CraftInventory right;

    public CardboardDoubleChestInventory(ChestBlockDoubleInventory block) {
        super(block.inventorylargechest);
        this.tile = block;
        this.left = new CraftInventory(block.inventorylargechest.first);
        this.right = new CraftInventory(block.inventorylargechest.second);
    }

    public CardboardDoubleChestInventory(DoubleInventory largeChest) {
        super(largeChest);

        left = (largeChest.first instanceof DoubleInventory) ? new CardboardDoubleChestInventory((DoubleInventory) largeChest.first) : new CraftInventory(largeChest.first);
        right = (largeChest.second instanceof DoubleInventory) ? new CardboardDoubleChestInventory((DoubleInventory) largeChest.second) : new CraftInventory(largeChest.second);
    }

    @Override
    public Inventory getLeftSide() {
        return left;
    }

    @Override
    public Inventory getRightSide() {
        return right;
    }

    @Override
    public void setContents(ItemStack[] items) {
        if (getInventory().size() < items.length)  throw new IllegalArgumentException("Invalid inventory size! expected <= " + getInventory().size());

        ItemStack[] leftItems = new ItemStack[left.getSize()], rightItems = new ItemStack[right.getSize()];
        System.arraycopy(items, 0, leftItems, 0, Math.min(left.getSize(), items.length));
        left.setContents(leftItems);
        if (items.length >= left.getSize()) {
            System.arraycopy(items, left.getSize(), rightItems, 0, Math.min(right.getSize(), items.length - left.getSize()));
            right.setContents(rightItems);
        }
    }

    @Override
    public DoubleChest getHolder() {
        return new DoubleChest(this);
    }

    @Override
    public Location getLocation() {
        return getLeftSide().getLocation().add(getRightSide().getLocation()).multiply(0.5);
    }

}