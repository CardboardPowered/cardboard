package org.cardboardpowered.impl.util;

import net.minecraft.entity.SpawnGroup;
import org.bukkit.entity.SpawnCategory;

public class CardboardSpawnCategory {

    public static boolean isValidForLimits(SpawnCategory spawnCategory) {
        return spawnCategory != null && spawnCategory.ordinal() < SpawnCategory.MISC.ordinal(); // Banner - use original
    }

    public static String getConfigNameSpawnLimit(SpawnCategory spawnCategory) {
        return switch (spawnCategory) {
            case MONSTER -> "spawn-limits.monsters";
            case ANIMAL -> "spawn-limits.animals";
            case WATER_ANIMAL -> "spawn-limits.water-animals";
            case WATER_AMBIENT -> "spawn-limits.water-ambient";
            case WATER_UNDERGROUND_CREATURE -> "spawn-limits.water-underground-creature";
            case AMBIENT -> "spawn-limits.ambient";
            case AXOLOTL -> "spawn-limits.axolotls";
            default -> throw new UnsupportedOperationException("Unknown Config value " + spawnCategory + " for spawn-limits");
        };
    }

    public static String getConfigNameTicksPerSpawn(SpawnCategory spawnCategory) {
        return switch (spawnCategory) {
            case MONSTER -> "ticks-per.monster-spawns";
            case ANIMAL -> "ticks-per.animal-spawns";
            case WATER_ANIMAL -> "ticks-per.water-spawns";
            case WATER_AMBIENT -> "ticks-per.water-ambient-spawns";
            case WATER_UNDERGROUND_CREATURE -> "ticks-per.water-underground-creature-spawns";
            case AMBIENT -> "ticks-per.ambient-spawns";
            case AXOLOTL -> "ticks-per.axolotl-spawns";
            default -> throw new UnsupportedOperationException("Unknown Config value " + spawnCategory + " for ticks-per");
        };
    }

    public static long getDefaultTicksPerSpawn(SpawnCategory spawnCategory) {
        return switch (spawnCategory) {
            case MONSTER, AXOLOTL, AMBIENT, WATER_UNDERGROUND_CREATURE, WATER_AMBIENT, WATER_ANIMAL -> 1;
            case ANIMAL -> 400; // This value come from commit 2995a08324f
            default -> throw new UnsupportedOperationException("Unknown Config value " + spawnCategory + " for ticks-per");
        };
    }

    public static SpawnCategory toBukkit(SpawnGroup enumCreatureType) {
        return switch (enumCreatureType) {
            case MONSTER -> SpawnCategory.MONSTER;
            case CREATURE -> SpawnCategory.ANIMAL;
            case AMBIENT -> SpawnCategory.AMBIENT;
            case AXOLOTLS -> SpawnCategory.AXOLOTL;
            case WATER_CREATURE -> SpawnCategory.WATER_ANIMAL;
            case WATER_AMBIENT -> SpawnCategory.WATER_AMBIENT;
            case UNDERGROUND_WATER_CREATURE -> SpawnCategory.WATER_UNDERGROUND_CREATURE;
            case MISC -> SpawnCategory.MISC;
            default -> SpawnCategory.valueOf(enumCreatureType.name());
        };
    }

    public static SpawnGroup toNMS(SpawnCategory spawnCategory) {
        return switch (spawnCategory) {
            case MONSTER -> SpawnGroup.MONSTER;
            case ANIMAL -> SpawnGroup.CREATURE;
            case AMBIENT -> SpawnGroup.AMBIENT;
            case AXOLOTL -> SpawnGroup.AXOLOTLS;
            case WATER_ANIMAL -> SpawnGroup.WATER_CREATURE;
            case WATER_AMBIENT -> SpawnGroup.WATER_AMBIENT;
            case WATER_UNDERGROUND_CREATURE -> SpawnGroup.UNDERGROUND_WATER_CREATURE;
            case MISC -> SpawnGroup.MISC;
            default -> SpawnGroup.valueOf(spawnCategory.name());
        };
    }

}