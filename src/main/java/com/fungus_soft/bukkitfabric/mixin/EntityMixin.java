package com.fungus_soft.bukkitfabric.mixin;

import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.fungus_soft.bukkitfabric.interfaces.IMixinCommandOutput;
import com.fungus_soft.bukkitfabric.interfaces.IMixinEntity;

import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

@Mixin(Entity.class)
public class EntityMixin implements CommandOutput, IMixinCommandOutput, IMixinEntity {

    private org.bukkit.entity.Entity bukkit;

    public EntityMixin() {
        this.bukkit = new CraftEntity((Entity) (Object) this);
    }

    @Override
    public boolean sendCommandFeedback() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void sendMessage(Text message) {
        ((Entity) (Object) this).sendMessage(message);
    }

    @Override
    public boolean shouldBroadcastConsoleToOps() {
        return CraftServer.server.shouldBroadcastConsoleToOps();
    }

    @Override
    public boolean shouldTrackOutput() {
        return false;
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