package org.cardboardpowered.impl.tag;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.item.Item;
//import net.minecraft.tag.TagGroup;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import org.bukkit.Material;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;

public class ItemTagImpl extends TagImpl<Item, Material> {

    /*public ItemTagImpl(TagGroup<Item> registry, Identifier tag) {
        super(registry, tag);
    }

    @Override
    public boolean isTagged(Material item) {
        return getHandle().contains(CraftMagicNumbers.getItem(item));
    }

    @Override
    public Set<Material> getValues() {
        return Collections.unmodifiableSet(getHandle().values().stream().map((item) -> CraftMagicNumbers.getMaterial(item)).collect(Collectors.toSet()));
    }*/
    
    public ItemTagImpl(Registry<Item> registry, TagKey<Item> tag) {
        super(registry, tag);
    }

    public boolean isTagged(Material item) {
        Item minecraft = CraftMagicNumbers.getItem(item);
        if (minecraft == null) {
            return false;
        }
        return minecraft.getRegistryEntry().isIn(this.tag);
    }

    public Set<Material> getValues() {
        return this.getHandle().stream().map(item -> CraftMagicNumbers.getMaterial((Item)item.value())).collect(Collectors.toUnmodifiableSet());
    }

}