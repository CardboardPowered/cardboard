package com.fungus_soft.bukkitfabric.mixin;

import org.bukkit.command.CommandSender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.fungus_soft.bukkitfabric.bukkitimpl.entity.FakePlayer;
import com.fungus_soft.bukkitfabric.interfaces.IMixinCommandOutput;

import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

@Mixin(ServerPlayerEntity.class)
public class PlayerMixin extends EntityMixin implements CommandOutput, IMixinCommandOutput {

    private FakePlayer bukkit;

    public PlayerMixin() {
        this.bukkit = new FakePlayer((ServerPlayerEntity) (Object) this);
    }

    @Override
    public boolean sendCommandFeedback() {
        return false;
    }

    @Override
    public boolean shouldBroadcastConsoleToOps() {
        return false;
    }

    @Override
    public boolean shouldTrackOutput() {
        return false;
    }

    @Inject(at = @At(value = "HEAD"), method = "tick()V")
    private void setBukkit(CallbackInfo callbackInfo) {
        if (null == bukkit)
            this.bukkit = new FakePlayer((ServerPlayerEntity) (Object) this);
    }

    @Override
    public CommandSender getBukkitSender(ServerCommandSource serverCommandSource) {
        return bukkit;
    }

}
