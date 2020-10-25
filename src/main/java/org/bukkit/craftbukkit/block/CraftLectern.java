package org.bukkit.craftbukkit.block;

import net.minecraft.block.LecternBlock;
import net.minecraft.block.entity.LecternBlockEntity;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Lectern;
import org.bukkit.inventory.Inventory;

import com.javazilla.bukkitfabric.impl.inventory.LecternInventoryImpl;

public class CraftLectern extends CraftBlockEntityState<LecternBlockEntity> implements Lectern {

    public CraftLectern(Block block) {
        super(block, LecternBlockEntity.class);
    }

    public CraftLectern(Material material, LecternBlockEntity te) {
        super(material, te);
    }

    @Override
    public int getPage() {
        return getSnapshot().getCurrentPage();
    }

    @Override
    public void setPage(int page) {
        getSnapshot().setCurrentPage(page);
    }

    @Override
    public Inventory getSnapshotInventory() {
        return new LecternInventoryImpl(this.getSnapshot().inventory);
    }

    @Override
    public Inventory getInventory() {
        return (!this.isPlaced()) ? this.getSnapshotInventory() : new LecternInventoryImpl(this.getTileEntity().inventory);
    }

    @Override
    public boolean update(boolean force, boolean applyPhysics) {
        boolean result = super.update(force, applyPhysics);
        if (result && this.isPlaced() && this.getType() == Material.LECTERN)
            LecternBlock.setPowered(this.world.getHandle(), this.getPosition(), this.getHandle());

        return result;
    }

}