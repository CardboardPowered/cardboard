package com.javazilla.bukkitfabric;

import java.io.IOException;
import java.util.Locale;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;

import com.google.common.base.Preconditions;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

public class MakeMaterial {

    public static void make() throws IOException {
        if (!FabricLoader.getInstance().isDevelopmentEnvironment())
            CraftMagicNumbers.test();
    }

    public static String standardize(Identifier location) {
        Preconditions.checkNotNull(location, "location");
        return (location.getNamespace().equals(NamespacedKey.MINECRAFT) ? location.getPath() : location.toString())
            .replace(':', '_')
            .replaceAll("\\s+", "_")
            .replaceAll("\\W", "")
            .toUpperCase(Locale.ENGLISH);
    }

}