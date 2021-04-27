package org.cardboardpowered.mixin.world;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.WorldAccess;

@Mixin(WorldAccess.class)
public interface MixinWorldAccess {

    default ServerWorld getMinecraftWorld() {
        return (ServerWorld)(WorldAccess)(Object)this;
    }

}