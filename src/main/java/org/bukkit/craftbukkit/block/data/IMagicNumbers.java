package org.bukkit.craftbukkit.block.data;

import net.minecraft.block.Block;
import org.bukkit.Material;

public interface IMagicNumbers {

    public Material IgetMaterial(Block b);
    
    public Block IgetBlock(Material m);

}