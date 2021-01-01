package org.cardboardpowered.impl.tag;

import java.util.HashMap;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.tag.TagGroup;
import net.minecraft.util.Identifier;
import org.bukkit.Material;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;

public class BlockTagImpl extends TagImpl<Block, Material> {

    public BlockTagImpl(TagGroup<Block> registry, Identifier tag) {
        super(registry, tag);
    }

    @Override
    public boolean isTagged(Material item) {
        try {
            return getHandle().contains(CraftMagicNumbers.getBlock(item));
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Set<Material> getValues() {
        HashMap<Material, Block> map = new HashMap<>();
        for (Block block : getHandle().values()) {
            try {
                map.put(CraftMagicNumbers.getMaterial(block), block);
            } catch (Exception e) {
            }
        }
        return map.keySet();
    }

}