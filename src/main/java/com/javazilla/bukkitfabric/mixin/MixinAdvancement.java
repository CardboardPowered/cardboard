package com.javazilla.bukkitfabric.mixin;

import org.cardboardpowered.impl.AdvancementImpl;
import org.spongepowered.asm.mixin.Mixin;

import com.javazilla.bukkitfabric.interfaces.IMixinAdvancement;

import net.minecraft.advancement.Advancement;

@Mixin(Advancement.class)
public class MixinAdvancement implements IMixinAdvancement {

    public AdvancementImpl bukkit = new AdvancementImpl((Advancement)(Object)this);

    @Override
    public AdvancementImpl getBukkitAdvancement() {
        return bukkit;
    }

}
