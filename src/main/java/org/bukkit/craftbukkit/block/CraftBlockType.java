package org.bukkit.craftbukkit.block;

import org.bukkit.Material;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;

import net.minecraft.block.Block;

public class CraftBlockType {

    public static Material minecraftToBukkit(Block block) {
        return CraftMagicNumbers.getMaterial(block);
    }

    public static Block bukkitToMinecraft(Material material) {
    	System.out.println(material);
        return CraftMagicNumbers.getBlock(material);
    }
}
