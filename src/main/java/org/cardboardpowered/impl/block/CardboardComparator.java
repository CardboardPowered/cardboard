package org.cardboardpowered.impl.block;

import net.minecraft.block.entity.ComparatorBlockEntity;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Comparator;

public class CardboardComparator extends CardboardBlockEntityState<ComparatorBlockEntity> implements Comparator {

    public CardboardComparator(final Block block) {
        super(block, ComparatorBlockEntity.class);
    }

    public CardboardComparator(final Material material, final ComparatorBlockEntity te) {
        super(material, te);
    }

}
