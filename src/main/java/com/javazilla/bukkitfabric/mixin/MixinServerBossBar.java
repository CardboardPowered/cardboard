package com.javazilla.bukkitfabric.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.javazilla.bukkitfabric.interfaces.IMixinServerBossBar;

import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.network.packet.s2c.play.BossBarS2CPacket.Type;

@Mixin(ServerBossBar.class)
public class MixinServerBossBar implements IMixinServerBossBar {

    @Override
    public void sendPacketBF(Type updateName) {
        sendPacket(updateName);
    }

    @Shadow
    public void sendPacket(Type updateName) {
    }

}
