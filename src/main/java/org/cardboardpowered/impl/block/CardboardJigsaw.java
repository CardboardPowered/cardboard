package org.cardboardpowered.impl.block;

import net.minecraft.block.entity.JigsawBlockEntity;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Jigsaw;
import org.bukkit.craftbukkit.block.CraftBlockEntityState;

public class CardboardJigsaw extends CraftBlockEntityState<JigsawBlockEntity> implements Jigsaw {

    public CardboardJigsaw(Block block) {
        super(block, JigsawBlockEntity.class);
    }

    public CardboardJigsaw(Material material, JigsawBlockEntity te) {
        super(material, te);
    }

}