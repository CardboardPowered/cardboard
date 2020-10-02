package com.javazilla.bukkitfabric.impl.tag;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.block.Block;
import net.minecraft.tag.TagGroup;
import net.minecraft.util.Identifier;
import org.bukkit.Material;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;

public class CraftBlockTag extends CraftTag<Block, Material> {

    public CraftBlockTag(TagGroup<Block> registry, Identifier tag) {
        super(registry, tag);
    }

    @Override
    public boolean isTagged(Material item) {
        return getHandle().contains(CraftMagicNumbers.getBlock(item));
    }

    @Override
    public Set<Material> getValues() {
        return Collections.unmodifiableSet(getHandle().values().stream().map((block) -> CraftMagicNumbers.getMaterial(block)).collect(Collectors.toSet()));
    }

}