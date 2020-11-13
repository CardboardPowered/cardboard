package org.cardboardpowered.impl.block;

import net.minecraft.block.entity.EnderChestBlockEntity;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.EnderChest;
import org.bukkit.craftbukkit.block.CraftBlockEntityState;

public class CardboardEnderchest extends CraftBlockEntityState<EnderChestBlockEntity> implements EnderChest {

    public CardboardEnderchest(final Block block) {
        super(block, EnderChestBlockEntity.class);
    }

    public CardboardEnderchest(final Material material, final EnderChestBlockEntity te) {
        super(material, te);
    }

}