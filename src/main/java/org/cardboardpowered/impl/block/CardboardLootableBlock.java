package org.cardboardpowered.impl.block;

import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.util.Identifier;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Nameable;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.block.CraftContainer;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.Lootable;

public abstract class CardboardLootableBlock<T extends LootableContainerBlockEntity> extends CraftContainer<T> implements Nameable, Lootable {

    public CardboardLootableBlock(Block block, Class<T> tileEntityClass) {
        super(block, tileEntityClass);
    }

    public CardboardLootableBlock(Material material, T tileEntity) {
        super(material, tileEntity);
    }

    @Override
    public void applyTo(T lootable) {
        super.applyTo(lootable);
        if (this.getSnapshot().lootTableId == null) lootable.setLootTable((Identifier) null, 0L);
    }

    @Override
    public LootTable getLootTable() {
        return (getSnapshot().lootTableId == null) ? null : Bukkit.getLootTable(CraftNamespacedKey.fromMinecraft(getSnapshot().lootTableId));
    }

    @Override
    public void setLootTable(LootTable table) {
        setLootTable(table, getSeed());
    }

    @Override
    public long getSeed() {
        return getSnapshot().lootTableSeed;
    }

    @Override
    public void setSeed(long seed) {
        setLootTable(getLootTable(), seed);
    }

    private void setLootTable(LootTable table, long seed) {
        getSnapshot().setLootTable(((table == null) ? null : CraftNamespacedKey.toMinecraft(table.getKey())), seed);
    }

}