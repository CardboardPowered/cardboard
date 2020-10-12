package com.javazilla.bukkitfabric.mixin.entity;

import org.bukkit.craftbukkit.CraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.impl.entity.ItemEntityImpl;

import net.minecraft.entity.ItemEntity;

@Mixin(ItemEntity.class)
public class MixinItemEntity extends MixinEntity {

    @Inject(at = @At(value = "HEAD"), method = "tick()V")
    private void setBukkit(CallbackInfo callbackInfo) {
        if (null == bukkit)
            this.bukkit = new ItemEntityImpl(CraftServer.INSTANCE, (ItemEntity) (Object) this, (ItemEntity) (Object) this);
    }

}