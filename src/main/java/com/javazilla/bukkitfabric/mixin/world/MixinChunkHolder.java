package com.javazilla.bukkitfabric.mixin.world;

import org.spongepowered.asm.mixin.Mixin;
import com.javazilla.bukkitfabric.interfaces.IMixinChunkHolder;
import net.minecraft.server.world.ChunkHolder;

@Mixin(ChunkHolder.class)
public class MixinChunkHolder implements IMixinChunkHolder {
}