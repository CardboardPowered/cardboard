package com.javazilla.bukkitfabric.impl.tag;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.craftbukkit.util.CraftMagicNumbers;

import net.minecraft.fluid.Fluid;
import net.minecraft.tag.TagGroup;
import net.minecraft.util.Identifier;

public class FluidTagImpl extends CraftTag<Fluid, org.bukkit.Fluid> {

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