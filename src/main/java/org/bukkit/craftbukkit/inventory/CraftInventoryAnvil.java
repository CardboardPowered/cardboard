package org.bukkit.craftbukkit.inventory;

import com.google.common.base.Preconditions;
import com.javazilla.bukkitfabric.interfaces.IMixinAnvilScreenHandler;

import net.minecraft.inventory.Inventory;
import net.minecraft.screen.AnvilScreenHandler;
import org.bukkit.Location;
import org.bukkit.inventory.AnvilInventory;

public class CraftInventoryAnvil extends CraftResultInventory implements AnvilInventory {

    private final Location location;
    private final AnvilScreenHandler container;

    public CraftInventoryAnvil(Location location, Inventory inventory, Inventory resultInventory, AnvilScreenHandler container) {
        super(inventory, resultInventory);
        this.location = location;
        this.container = container;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public String getRenameText() {
        return ((IMixinAnvilScreenHandler)container).getNewItemName_BF();
    }

    @Override
    public int getRepairCost() {
        return ((IMixinAnvilScreenHandler)container).getLevelCost_BF();
    }

    @Override
    public void setRepairCost(int i) {
        ((IMixinAnvilScreenHandler)container).setLevelCost_BF(i);
    }

    @Override
    public int getMaximumRepairCost() {
        return ((IMixinAnvilScreenHandler)container).getMaxRepairCost_BF();
    }

    @Override
    public void setMaximumRepairCost(int levels) {
        Preconditions.checkArgument(levels >= 0, "Maximum repair cost must be positive (or 0)");
        ((IMixinAnvilScreenHandler)container).setMaxRepairCost_BF(levels);
    }

}