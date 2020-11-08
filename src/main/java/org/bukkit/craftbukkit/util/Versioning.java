package org.bukkit.craftbukkit.util;

import org.bukkit.Bukkit;

@Deprecated
public final class Versioning {

    public static String getBukkitVersion() {
        return Bukkit.getVersion(); // Returns: git-BukkitFabric-GITHASH
    }

}