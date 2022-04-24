package org.cardboardpowered.impl.tag;

import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.block.Block;
//import net.minecraft.tag.TagGroup;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import org.bukkit.Material;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;

public class BlockTagImpl extends TagImpl<Block, Material> {

    /*public BlockTagImpl(TagGroup<Block> registry, Identifier tag) {
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
    }*/
    
    public BlockTagImpl(Registry<Block> registry, TagKey<Block> tag) {
        super(registry, tag);
    }

    public boolean isTagged(Material item) {
        Block block = CraftMagicNumbers.getBlock(item);
        if (block == null) {
            return false;
        }
        return block.getRegistryEntry().isIn(this.tag);
    }

    public Set<Material> getValues() {
        return this.getHandle().stream().map(block -> CraftMagicNumbers.getMaterial((Block)block.value())).collect(Collectors.toUnmodifiableSet());
    }

}