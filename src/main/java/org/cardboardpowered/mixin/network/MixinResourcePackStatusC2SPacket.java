package org.cardboardpowered.mixin.network;

import com.javazilla.bukkitfabric.interfaces.IMixinResourcePackStatusC2SPacket;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket.Status;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ResourcePackStatusC2SPacket.class)
public class MixinResourcePackStatusC2SPacket implements IMixinResourcePackStatusC2SPacket {

    @Shadow
    public Status status;

    @Override
    public Status getStatus_Bukkit() {
        return status;
    }

}
