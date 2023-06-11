package org.cardboardpowered.impl.tag;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;

import net.minecraft.fluid.Fluid;
// 1.18.1: import net.minecraft.tag.TagGroup;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;

import net.minecraft.registry.RegistryKey;

public abstract class TagImpl<N, B extends Keyed> implements Tag<B> {

    /*
    Old 1.18.1:
    private final TagGroup<N> registry;
    private final Identifier tag;
    private net.minecraft.tag.Tag<N> handle;

    public TagImpl(TagGroup<N> registry, Identifier tag) {
        this.registry = registry;
        this.tag = tag;
    }

    public net.minecraft.tag.Tag<N> getHandle() {
        return (handle == null) ? (handle = registry.getTagOrEmpty(tag)) : handle;
    }

    @Override
    public NamespacedKey getKey() {
        return CraftNamespacedKey.fromMinecraft(tag);
    }*/
    
    protected final net.minecraft.registry.Registry<N> registry;
    protected final TagKey<N> tag;
    //
    private RegistryEntryList.Named<N> handle;

    public TagImpl(Registry<N> registry, TagKey<N> tag) {
        this.registry = registry;
        this.tag = tag;
        this.handle = registry.getEntryList(this.tag).orElseThrow();
    }

    protected RegistryEntryList.Named<N> getHandle() {
        return handle;
    }

    @Override
    public NamespacedKey getKey() {
        return CraftNamespacedKey.fromMinecraft(tag.id());
    }



}