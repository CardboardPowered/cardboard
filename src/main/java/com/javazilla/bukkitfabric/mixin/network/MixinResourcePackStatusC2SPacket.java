package com.javazilla.bukkitfabric.mixin.network;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.javazilla.bukkitfabric.interfaces.IMixinResourcePackStatusC2SPacket;

import net.minecraft.network.packet.c2s.play.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.c2s.play.ResourcePackStatusC2SPacket.Status;

@Mixin(ResourcePackStatusC2SPacket.class)
public class MixinResourcePackStatusC2SPacket implements IMixinResourcePackStatusC2SPacket {

    @Shadow
    public Status status;

    @Override
    public Status getStatus_Bukkit() {
        return status;
    }

}
