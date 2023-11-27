package com.javazilla.bukkitfabric.interfaces;

import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket.Status;

public interface IMixinResourcePackStatusC2SPacket {

    public Status getStatus_Bukkit();

}
