package io.github.essentialsx.itemdbgenerator.providers.item;

import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

public class MaterialFallbacks {
    private static final Map<Material, String[]> MAP = new HashMap<>();

    static {
        /* New "smooth stone" added in 1.14 */
        add(Material.SMOOTH_STONE, Material.STONE);
        /* Dyes added in 1.14 */
        add(Material.BLACK_DYE, Material.INK_SAC, "INK_SACK");
        add(Material.BROWN_DYE, Material.COCOA_BEANS);
        add(Material.BLUE_DYE, Material.LAPIS_LAZULI);
        add(Material.WHITE_DYE, Material.BONE_MEAL);
        /* Dyes renamed in 1.14 */
        add(Material.RED_DYE, "ROSE_RED");
        add(Material.YELLOW_DYE, "DANDELION_YELLOW");
        add(Material.GREEN_DYE, "CACTUS_GREEN");
        /* Signs added in 1.14 */
        add(Material.ACACIA_SIGN, "SIGN");
        add(Material.BIRCH_SIGN, "SIGN");
        add(Material.DARK_OAK_SIGN, "SIGN");
        add(Material.JUNGLE_SIGN, "SIGN");
        add(Material.OAK_SIGN, "SIGN");
        add(Material.SPRUCE_SIGN, "SIGN");
        /* 1.16: zombie pigmen -> zombified piglins */
        //add(Material.ZOMBIFIED_PIGLIN_SPAWN_EGG, "ZOMBIE_PIGMAN_SPAWN_EGG"); TODO: uncomment when 1.16 API
    }

    private static void add(Material material, Object... fallbacks) {
        String[] fallbackStrings = new String[fallbacks.length];
        for (int i = 0; i < fallbacks.length; i++) {
            Object fallbackObj = fallbacks[i];
            if (fallbackObj instanceof Material) {
                fallbackStrings[i] = ((Material) fallbackObj).name();
            } else {
                fallbackStrings[i] = (String) fallbackObj;
            }
        }

        MAP.put(material, fallbackStrings);
    }

    public static String[] get(Material material) {
        return MAP.get(material);
    }
}
