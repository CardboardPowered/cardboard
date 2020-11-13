package org.cardboardpowered.impl.block;

import net.minecraft.block.entity.ComparatorBlockEntity;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Comparator;
import org.bukkit.craftbukkit.block.CraftBlockEntityState;

public class CardboardComparator extends CraftBlockEntityState<ComparatorBlockEntity> implements Comparator {

    public CardboardComparator(final Block block) {
        super(block, ComparatorBlockEntity.class);
    }

    public CardboardComparator(final Material material, final ComparatorBlockEntity te) {
        super(material, te);
    }

}
