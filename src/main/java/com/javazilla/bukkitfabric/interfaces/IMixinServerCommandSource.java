package com.javazilla.bukkitfabric.interfaces;

import org.bukkit.command.CommandSender;

public interface IMixinServerCommandSource {

    CommandSender getBukkitSender();

}