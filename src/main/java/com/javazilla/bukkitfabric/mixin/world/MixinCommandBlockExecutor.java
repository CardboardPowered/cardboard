package com.javazilla.bukkitfabric.mixin.world;

import org.bukkit.command.CommandSender;
import org.spongepowered.asm.mixin.Mixin;

import com.javazilla.bukkitfabric.interfaces.IMixinCommandOutput;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.world.CommandBlockExecutor;

@Mixin(CommandBlockExecutor.class)
public abstract class MixinCommandBlockExecutor implements IMixinCommandOutput {

    @Override
    public abstract CommandSender getBukkitSender(ServerCommandSource wrapper);

}