package com.fungus_soft.bukkitfabric.interfaces;

import org.bukkit.command.CommandSender;

import net.minecraft.server.command.ServerCommandSource;

public interface IMixinCommandOutput {

    CommandSender getBukkitSender(ServerCommandSource serverCommandSource);

}