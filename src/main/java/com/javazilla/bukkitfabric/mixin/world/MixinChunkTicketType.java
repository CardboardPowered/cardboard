package com.javazilla.bukkitfabric.mixin.world;

import java.util.Comparator;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.javazilla.bukkitfabric.interfaces.IMixinChunkTicketType;

import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.util.Unit;

@Mixin(ChunkTicketType.class)
public class MixinChunkTicketType implements IMixinChunkTicketType {

    private static final ChunkTicketType<Unit> PLUGIN = create("plugin", (a, b) -> 0);

    @Override
    public ChunkTicketType<Unit> getBukkitPluginTicketType() {
        return PLUGIN;
    }

    @Shadow
    public static <T> ChunkTicketType<T> create(String s, Comparator<T> comparator) {
        return null;
    }

}