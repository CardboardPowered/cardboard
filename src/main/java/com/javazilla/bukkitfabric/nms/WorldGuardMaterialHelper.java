package com.javazilla.bukkitfabric.nms;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.EntityType;

public class WorldGuardMaterialHelper {

    /**
     * @see {@link com.sk89q.worldguard.bukkit.util.Materials}
     */
    public static boolean isSpawnEgg(Material mat) {
        return (mat.name().contains("SPAWN_EGG"));
    }

    /**
     * @see {@link com.sk89q.worldguard.bukkit.util.Materials}
     */
    public static boolean isArmor(Material mat) {
        String n = mat.name();
        return (n.contains("HELMENT")||n.contains("CHESTPLATE")||n.contains("LEGGINGS")||n.contains("BOOTS")) || mat==Material.ELYTRA;
    }

    /**
     * @see {@link com.sk89q.worldguard.bukkit.util.Materials}
     */
    public static boolean isToolApplicable(Material tool, Material target) {
        String tooln = tool.name();
        String targn = target.name();
        
        if (tooln.contains("HOE"))
            return (targn.contains("GRASS_BLOCK") || (targn.contains("DIRT") && !targn.contains("COARSE")));
 
        if (tooln.contains("_AXE")) {
            if (targn.contains("WAXED") || targn.contains("_LOG") && targn.contains("_WOOD"))
                return true;
            if (target == Material.CRIMSON_STEM || target == Material.WARPED_STEM ||
                    target == Material.CRIMSON_HYPHAE || target == Material.WARPED_HYPHAE)
                return true;
            return false;
        }
        
        if (tooln.contains("INK_SAC") || tooln.contains("_DYE"))
            return Tag.SIGNS.isTagged(target);
        
        if (tool == Material.HONEYCOMB) return targn.contains("COPPER") && !targn.contains("WAXED");
        if (tool == Material.SHEARS) return (target == Material.PUMPKIN || target == Material.BEE_NEST || target == Material.BEEHIVE);

        if (tooln.contains("_SHOVEL"))
            return target == Material.GRASS_BLOCK || target == Material.CAMPFIRE || target == Material.SOUL_CAMPFIRE;
        return false;
    }

    /**
     * @see {@link com.sk89q.worldguard.bukkit.util.Materials}
     */
    public static EntityType getEntitySpawnEgg(Material mat) {
        String name = mat.name().replace("_SPAWN_EGG", "");
        try {
            return EntityType.valueOf(name);
        } catch (Exception e) {
            return EntityType.PIG;
        }
    }

    /**
     * @see {@link com.sk89q.worldguard.bukkit.util.Materials}
     */
    public static boolean isWaxedCopper(Material type) {
        return type.name().contains("COPPER") && type.name().contains("WAX");
    }

}