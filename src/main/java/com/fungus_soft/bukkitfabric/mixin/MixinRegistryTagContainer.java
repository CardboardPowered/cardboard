package com.fungus_soft.bukkitfabric.mixin;

import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.tag.TagGroup;

@Mixin(TagGroup.class)
public interface MixinRegistryTagContainer {

    // TODO: Removed in Bukkit 1.16.2

}