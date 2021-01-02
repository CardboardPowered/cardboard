package com.javazilla.bukkitfabric.interfaces;

import net.minecraft.network.packet.c2s.play.ResourcePackStatusC2SPacket;

public interface IMixinResourcePackStatusC2SPacket {

    public ResourcePackStatusC2SPacket.Status getStatus_Bukkit();

}