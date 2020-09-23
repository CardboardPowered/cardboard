package com.javazilla.bukkitfabric.mixin;

import org.bukkit.craftbukkit.advancement.CraftAdvancement;
import org.spongepowered.asm.mixin.Mixin;

import com.javazilla.bukkitfabric.interfaces.IMixinAdvancement;

import net.minecraft.advancement.Advancement;

@Mixin(Advancement.class)
public class MixinAdvancement implements IMixinAdvancement {

    public CraftAdvancement bukkit = new CraftAdvancement((Advancement)(Object)this);

    @Override
    public CraftAdvancement getBukkitAdvancement() {
        return bukkit;
    }

}
