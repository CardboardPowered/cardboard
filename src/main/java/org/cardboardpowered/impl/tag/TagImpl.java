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

    /**
     * Tag for Fluids
     */
    public class FluidTagImpl extends TagImpl<Fluid, org.bukkit.Fluid> {

        public FluidTagImpl(TagGroup<Fluid> registry, Identifier tag) {
            super(registry, tag);
        }

        @Override
        public boolean isTagged(org.bukkit.Fluid fluid) {
            return getHandle().contains(CraftMagicNumbers.getFluid(fluid));
        }

        @Override
        public Set<org.bukkit.Fluid> getValues() {
            return Collections.unmodifiableSet(getHandle().values().stream().map(CraftMagicNumbers::getFluid).collect(Collectors.toSet()));
        }

    }

}