package org.cardboardpowered.impl.util;

import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.util.permissions.DefaultPermissions;

import static org.bukkit.util.permissions.DefaultPermissions.registerPermission;

public final class CommandPermissions {

    private static final String PREFIX = "minecraft.command.";

    private CommandPermissions() {}

    public static Permission registerPermissions(Permission parent) {
        Permission commands = DefaultPermissions.registerPermission("minecraft.command", "Gives the user the ability to use all vanilla commands", parent);

        registerPermission(PREFIX + "kill", "Allows the user to kill themselves", PermissionDefault.OP, commands);
        registerPermission(PREFIX + "me", "Allows an alternative chat method", PermissionDefault.TRUE, commands);
        registerPermission(PREFIX + "msg", "Allows private messaging", PermissionDefault.TRUE, commands);
        registerPermission(PREFIX + "help", "Allows use of /help", PermissionDefault.TRUE, commands);
        registerPermission(PREFIX + "say", "Allows the user to talk as the console", PermissionDefault.OP, commands);
        registerPermission(PREFIX + "give", "Allows the user to give items to players", PermissionDefault.OP, commands);
        registerPermission(PREFIX + "teleport", "Allows the user to teleport players", PermissionDefault.OP, commands);
        registerPermission(PREFIX + "kick", "Allows the user to kick players", PermissionDefault.OP, commands);
        registerPermission(PREFIX + "stop", "Allows the user to stop the server", PermissionDefault.OP, commands);
        registerPermission(PREFIX + "list", "Allows the user to list all online players", PermissionDefault.OP, commands);
        registerPermission(PREFIX + "gamemode", "Allows the user to change the gamemode of another player", PermissionDefault.OP, commands);
        registerPermission(PREFIX + "xp", "Allows the user to give themselves or others arbitrary values of experience", PermissionDefault.OP, commands);
        registerPermission(PREFIX + "toggledownfall", "Allows the user to toggle rain on/off for a given world", PermissionDefault.OP, commands);
        registerPermission(PREFIX + "defaultgamemode", "Allows the user to change the default gamemode of the server", PermissionDefault.OP, commands);
        registerPermission(PREFIX + "seed", "Allows the user to view the seed of the world", PermissionDefault.OP, commands);
        registerPermission(PREFIX + "effect", "Allows the user to add/remove effects on players", PermissionDefault.OP, commands);
        registerPermission(PREFIX + "selector", "Allows the use of selectors", PermissionDefault.OP, commands);
        registerPermission(PREFIX + "trigger", "Allows the use of the trigger command", PermissionDefault.TRUE, commands);

        registerPermission("minecraft.admin.command_feedback", "Receive command broadcasts when sendCommandFeedback is true", PermissionDefault.OP, commands);

        commands.recalculatePermissibles();
        return commands;
    }

    public static void registerCorePermissions() {
        Permission parent = DefaultPermissions.registerPermission("minecraft", "Gives the user the ability to use all vanilla utilities and commands");
        registerPermissions(parent);
        parent.recalculatePermissibles();
    }

}