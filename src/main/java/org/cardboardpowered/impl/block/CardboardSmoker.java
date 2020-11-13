package org.cardboardpowered.impl.block;

import net.minecraft.block.entity.SmokerBlockEntity;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Smoker;

public class CardboardSmoker extends CardboardFurnace implements Smoker {

    public CardboardSmoker(Block block) {
        super(block, SmokerBlockEntity.class);
    }

    public CardboardSmoker(Material material, SmokerBlockEntity te) {
        super(material, te);
    }

}