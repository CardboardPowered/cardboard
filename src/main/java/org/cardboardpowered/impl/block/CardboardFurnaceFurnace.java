package org.cardboardpowered.impl.block;

import net.minecraft.block.entity.FurnaceBlockEntity;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class CardboardFurnaceFurnace extends CardboardFurnace {

    public CardboardFurnaceFurnace(Block block) {
        super(block, FurnaceBlockEntity.class);
    }

    public CardboardFurnaceFurnace(Material material, FurnaceBlockEntity te) {
        super(material, te);
    }

}