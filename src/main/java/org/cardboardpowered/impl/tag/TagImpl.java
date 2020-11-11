package org.cardboardpowered.impl.tag;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;

import net.minecraft.tag.TagGroup;
import net.minecraft.util.Identifier;

public abstract class TagImpl<N, B extends Keyed> implements Tag<B> {

    private final TagGroup<N> registry;
    private final Identifier tag;
    private net.minecraft.tag.Tag<N> handle;

    public TagImpl(TagGroup<N> registry, Identifier tag) {
        this.registry = registry;
        this.tag = tag;
    }

    public net.minecraft.tag.Tag<N> getHandle() {
        return (handle == null) ? handle = registry.getTag(tag) : handle;
    }

    @Override
    public NamespacedKey getKey() {
        return CraftNamespacedKey.fromMinecraft(tag);
    }

}