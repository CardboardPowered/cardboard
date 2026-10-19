package org.cardboardpowered.util;

import java.lang.reflect.Method;

import net.minecraft.world.entity.Entity;

/**
 * Lets Bukkit plugins see permissions from a Fabric permissions provider (e.g. LuckPerms-Fabric).
 * Called by reflection because fabric-permissions-api is only present when such a mod bundles it;
 * callers already gate on FabricLoader.isModLoaded("fabric-permissions-api-v0").
 */
public class PermissionHook {

    private static Method check;
    private static boolean lookupFailed;

    public static boolean hasPermission(Entity e, String permission) {
        Method m = checkMethod();
        if (m == null) return false;
        try {
            return (boolean) m.invoke(null, e, permission);
        } catch (ReflectiveOperationException ex) {
            return false;
        }
    }

    private static Method checkMethod() {
        if (check == null && !lookupFailed) {
            try {
                check = Class.forName("me.lucko.fabric.api.permissions.v0.Permissions")
                        .getMethod("check", Entity.class, String.class);
            } catch (ReflectiveOperationException ex) {
                lookupFailed = true;
            }
        }
        return check;
    }

}