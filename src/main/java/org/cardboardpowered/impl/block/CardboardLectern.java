package org.cardboardpowered.impl.block;

import net.minecraft.block.LecternBlock;
import net.minecraft.block.entity.LecternBlockEntity;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Lectern;
import org.bukkit.craftbukkit.block.CraftBlockEntityState;
import org.bukkit.inventory.Inventory;
import org.cardboardpowered.impl.inventory.CardboardLecternInventory;

public class CardboardLectern extends CraftBlockEntityState<LecternBlockEntity> implements Lectern {

    public CardboardLectern(Block block) {
        super(block, LecternBlockEntity.class);
    }

    public CardboardLectern(Material material, LecternBlockEntity te) {
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
        return new CardboardLecternInventory(this.getSnapshot().inventory);
    }

    @Override
    public Inventory getInventory() {
        return (!this.isPlaced()) ? this.getSnapshotInventory() : new CardboardLecternInventory(this.getTileEntity().inventory);
    }

    @Override
    public boolean update(boolean force, boolean applyPhysics) {
        boolean result = super.update(force, applyPhysics);
        if (result && this.isPlaced() && this.getType() == Material.LECTERN)
            LecternBlock.setPowered(this.world.getHandle(), this.getPosition(), this.getHandle());
        return result;
    }

}