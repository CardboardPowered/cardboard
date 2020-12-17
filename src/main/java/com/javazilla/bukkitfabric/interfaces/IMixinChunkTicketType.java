package com.javazilla.bukkitfabric.interfaces;

import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.util.Unit;

public interface IMixinChunkTicketType {

    public ChunkTicketType<Unit> getBukkitPluginTicketType();

}