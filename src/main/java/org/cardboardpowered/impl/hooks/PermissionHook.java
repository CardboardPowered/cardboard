package org.cardboardpowered.impl.hooks;

import java.util.HashMap;

import org.bukkit.Bukkit;

import cyber.permissions.v1.CyberPermissions;
import cyber.permissions.v1.Permissible;
import cyber.permissions.v1.Permission;
import cyber.permissions.v1.PermissionDefaults;
import net.minecraft.server.network.ServerPlayerEntity;

public class PermissionHook {

    public static HashMap<String, Permission> perms = new HashMap<>();

    public static boolean hasPermission(ServerPlayerEntity plr, String permission) {
        if (null == perms.get(permission)) {
            org.bukkit.permissions.Permission bu = Bukkit.getPluginManager().getPermission(permission);
            if (null == bu)
                return false;
            PermissionDefaults def = PermissionDefaults.OPERATOR;
            switch (bu.getDefault()) {
                case FALSE:
                    def = PermissionDefaults.FALSE;
                    break;
                case NOT_OP:
                    def = PermissionDefaults.NON_OPERATOR;
                    break;
                case OP:
                    def = PermissionDefaults.OPERATOR;
                    break;
                case TRUE:
                    def = PermissionDefaults.TRUE;
                    break;
                default:
                    break;
                
            }
            Permission cy = new Permission(permission, bu.getDescription(), def);
            perms.put(permission, cy);
        }

        Permission cyber = perms.get(permission);
        Permissible perm = CyberPermissions.getPermissible(plr);

        return perm.hasPermission(cyber);
    }

}
