package com.javazilla.bukkitfabric.mixin.world;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.interfaces.IMixinEntity;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.world.EntityLoader;

@Mixin(ServerChunkManager.class)
public class MixinEntityLoader {

    @Inject(at = @At("TAIL"), method = "unloadEntity")
    public void unvalidateEntityBF(Entity entity, CallbackInfo ci) {
        ((IMixinEntity)entity).setValid(false);
    } 

    @Inject(at = @At("TAIL"), method = "loadEntity")
    public void validateEntityBF(Entity entity, CallbackInfo ci) {
       // if (!this.inEntityTick) {
            IMixinEntity bf = (IMixinEntity) entity;
            bf.setValid(true);
            if (null == bf.getOriginBF() && null != bf.getBukkitEntity()) {
                // Paper's Entity Origin API
                bf.setOriginBF(bf.getBukkitEntity().getLocation());
            }
      //  }
    } 

}