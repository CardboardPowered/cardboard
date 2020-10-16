package com.javazilla.bukkitfabric.interfaces;

import net.minecraft.network.packet.s2c.play.BossBarS2CPacket.Type;

public interface IMixinServerBossBar {

    public void sendPacketBF(Type updateName);

}