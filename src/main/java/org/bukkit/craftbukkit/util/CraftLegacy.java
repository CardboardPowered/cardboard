package org.bukkit.craftbukkit.util;

import java.util.Arrays;
import org.bukkit.Material;
import org.bukkit.material.MaterialData;

@Deprecated
public final class CraftLegacy {

    private CraftLegacy() {
    }

    public static Material fromLegacy(Material material) {
        if (material == null || !material.isLegacy()) return material;
        return CraftLegacyMaterials.fromLegacy(material);
    }

    public static Material fromLegacy(MaterialData materialData) {
        return CraftLegacyMaterials.fromLegacy(materialData);
    }

    public static Material[] modern_values() {
        Material[] values = Material.values();
        return Arrays.copyOfRange(values, 0, Material.LEGACY_AIR.ordinal());
    }

    public static int modern_ordinal(Material material) {
        if (material.isLegacy()) throw new NoSuchFieldError("Legacy field ordinal: " + material); // SPIGOT-4002
        return material.ordinal();
    }

}