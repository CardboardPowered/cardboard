package org.cardboardpowered.impl.block;

import net.minecraft.block.entity.BellBlockEntity;
import org.bukkit.Material;
import org.bukkit.block.Bell;
import org.bukkit.block.Block;

public class CardboardBell extends CardboardBlockEntityState<BellBlockEntity> implements Bell {

    public CardboardBell(Block block) {
        super(block, BellBlockEntity.class);
    }

    public CardboardBell(Material material, BellBlockEntity te) {
        super(material, te);
    }

}