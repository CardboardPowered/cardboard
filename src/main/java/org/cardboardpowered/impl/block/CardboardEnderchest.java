package org.cardboardpowered.impl.block;

import net.minecraft.block.entity.EnderChestBlockEntity;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.EnderChest;

public class CardboardEnderchest extends CardboardBlockEntityState<EnderChestBlockEntity> implements EnderChest {

    public CardboardEnderchest(final Block block) {
        super(block, EnderChestBlockEntity.class);
    }

    public CardboardEnderchest(final Material material, final EnderChestBlockEntity te) {
        super(material, te);
    }

    @Override
    public void close() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean isOpen() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void open() {
        // TODO Auto-generated method stub
        
    }

}