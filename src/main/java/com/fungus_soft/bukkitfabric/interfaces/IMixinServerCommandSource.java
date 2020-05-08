package com.fungus_soft.bukkitfabric.interfaces;

import org.bukkit.command.CommandSender;

public interface IMixinServerCommandSource {

    CommandSender getBukkitSender();

}