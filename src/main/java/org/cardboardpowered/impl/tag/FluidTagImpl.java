package org.cardboardpowered.impl.tag;

import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Tag;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;

import net.minecraft.fluid.Fluid;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public class FluidTagImpl extends TagImpl<Fluid, org.bukkit.Fluid> {

    public FluidTagImpl(Registry<Fluid> registry, TagKey<Fluid> tag) {
        super(registry, tag);
    }

    public boolean isTagged(org.bukkit.Fluid fluid) {
        return this.registry.entryOf(RegistryKey.of(RegistryKeys.FLUID, CraftNamespacedKey.toMinecraft(fluid.getKey()))).isIn(this.tag);
    }

    public Set<org.bukkit.Fluid> getValues() {
        return this.getHandle().stream().map(fluid -> CraftMagicNumbers.getFluid((Fluid)fluid.value())).collect(Collectors.toUnmodifiableSet());
    }

}