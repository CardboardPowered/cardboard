package org.cardboardpowered.impl.block;

import net.minecraft.block.entity.ConduitBlockEntity;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Conduit;
import org.bukkit.craftbukkit.block.CraftBlockEntityState;

public class CardboardConduit extends CraftBlockEntityState<ConduitBlockEntity> implements Conduit {

    public CardboardConduit(Block block) {
        super(block, ConduitBlockEntity.class);
    }

    public CardboardConduit(Material material, ConduitBlockEntity te) {
        super(material, te);
    }

}