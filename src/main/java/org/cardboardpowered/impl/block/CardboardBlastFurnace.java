package org.cardboardpowered.impl.block;

import net.minecraft.block.entity.BlastFurnaceBlockEntity;
import org.bukkit.Material;
import org.bukkit.block.BlastFurnace;
import org.bukkit.block.Block;

public class CardboardBlastFurnace extends CardboardFurnace implements BlastFurnace {

    public CardboardBlastFurnace(Block block) {
        super(block, BlastFurnaceBlockEntity.class);
    }

    public CardboardBlastFurnace(Material material, BlastFurnaceBlockEntity te) {
        super(material, te);
    }

}
