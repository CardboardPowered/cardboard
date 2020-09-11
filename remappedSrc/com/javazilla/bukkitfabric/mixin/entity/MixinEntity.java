package com.javazilla.bukkitfabric.mixin.entity;

import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.interfaces.IMixinCommandOutput;
import com.javazilla.bukkitfabric.interfaces.IMixinEntity;

import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

@Mixin(Entity.class)
public class MixinEntity implements IMixinCommandOutput, IMixinEntity {

    public org.bukkit.entity.Entity bukkit;
    public org.bukkit.projectiles.ProjectileSource projectileSource;

    public MixinEntity() {
        this.bukkit = new CraftEntity((Entity) (Object) this);
    }

    public void sendSystemMessage(Text message) {
        ((Entity) (Object) this).sendSystemMessage(message, UUID.randomUUID());
    }

    @Inject(at = @At(value = "HEAD"), method = "tick()V")
    private void setBukkit(CallbackInfo callbackInfo) {
        if (null == bukkit)
            this.bukkit = new CraftEntity((Entity) (Object) this);
    }

    @Override
    public CommandSender getBukkitSender(ServerCommandSource serverCommandSource) {
        return bukkit;
    }

    @Override
    public org.bukkit.entity.Entity getBukkitEntity() {
        return bukkit;
    }

}