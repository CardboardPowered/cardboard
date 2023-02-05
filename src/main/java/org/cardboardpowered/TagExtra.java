package org.cardboardpowered;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionEffectTypeWrapper;

public class TagExtra {

	/**
     * Vanilla block tag representing all colors of carpet.
     */
    public static Tag<Material> WOOL_CARPETS = Bukkit.getTag(Tag.REGISTRY_BLOCKS, NamespacedKey.minecraft("wool_carpets"), Material.class);
    /**
     * @deprecated {@link #WOOL_CARPETS}.
     */
    @Deprecated
    public static Tag<Material> CARPETS = WOOL_CARPETS;
    
    /**
     * Vanilla block tag representing all blocks which reset fall damage.
     */
    public static Tag<Material> FALL_DAMAGE_RESETTING = Bukkit.getTag(Tag.REGISTRY_BLOCKS, NamespacedKey.minecraft("fall_damage_resetting"), Material.class);
    
    // ORES:
    public static Tag<Material> COAL_ORES = Bukkit.getTag(Tag.REGISTRY_BLOCKS, NamespacedKey.minecraft("coal_ores"), Material.class);
    public static Tag<Material> IRON_ORES = Bukkit.getTag(Tag.REGISTRY_BLOCKS, NamespacedKey.minecraft("iron_ores"), Material.class);
    public static Tag<Material> DIAMOND_ORES = Bukkit.getTag(Tag.REGISTRY_BLOCKS, NamespacedKey.minecraft("diamond_ores"), Material.class);
    public static Tag<Material> REDSTONE_ORES = Bukkit.getTag(Tag.REGISTRY_BLOCKS, NamespacedKey.minecraft("redstone_ores"), Material.class);
    public static Tag<Material> EMERALD_ORES = Bukkit.getTag(Tag.REGISTRY_BLOCKS, NamespacedKey.minecraft("emerald_ores"), Material.class);
    public static Tag<Material> COPPER_ORES = Bukkit.getTag(Tag.REGISTRY_BLOCKS, NamespacedKey.minecraft("copper_ores"), Material.class);
    public static Tag<Material> LAPIS_ORES = Bukkit.getTag(Tag.REGISTRY_BLOCKS, NamespacedKey.minecraft("lapis_ores"), Material.class);
    public static Tag<Material> CANDLES = Bukkit.getTag(Tag.REGISTRY_BLOCKS, NamespacedKey.minecraft("candles"), Material.class);
    public static Tag<Material> CANDLE_CAKES = Bukkit.getTag(Tag.REGISTRY_BLOCKS, NamespacedKey.minecraft("candle_cakes"), Material.class);
    public static Tag<Material> CAULDRONS = Bukkit.getTag(Tag.REGISTRY_BLOCKS, NamespacedKey.minecraft("cauldrons"), Material.class);
    public static Tag<Material> ITEMS_CHEST_BOATS = Bukkit.getTag(Tag.REGISTRY_ITEMS, NamespacedKey.minecraft("chest_boats"), Material.class);
    
    
    public void test() {
    	// Tag.WOOL_CARPETS.getValues();
    	// Tag.COAL_ORES.getValues();
    	//Tag.IRON_ORES.getValues();
    	//Tag.DIAMOND_ORES.getValues();
    	//Tag.REDSTONE_ORES.getValues();
    	//Tag.COPPER_ORES.getValues();
    	//Tag.EMERALD_ORES.getValues();
    	//Tag.LAPIS_ORES.getValues();
    	//Tag.CANDLES.getValues();
    	//Tag.CANDLE_CAKES.getValues();
    	//Tag.CAULDRONS.getValues();
    }
    
    /**
     * Causes the player's vision to dim occasionally.
     */
    public static final PotionEffectType DARKNESS = new ExtraPotionEffectTypeWrapper(33, "darkness");

    
}