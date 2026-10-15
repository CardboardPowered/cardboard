package org.cardboardpowered;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import org.bukkit.entity.EntityType;

import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.potion.PotionType;

import org.cardboardpowered.bridge.bukkit.entity.BukkitEntityTypeBridge;

import io.izzel.arclight.api.EnumHelper;

/**
 register_entities * Registry API Util
 *
 * @since 1.21.4
 */
public class RegistryUtil {

	private static final Map<String, EntityType> CUSTOM_ENTITY_NAME_MAP = new HashMap<String, EntityType>();
    private static final Map<Short, EntityType> CUSTOM_ENTITY_ID_MAP = new HashMap<Short, EntityType>();
	
    public static Map<net.minecraft.world.entity.EntityType<?>, EntityType> MODDED_ENTITIES_MAP = new ConcurrentHashMap<>();
    
	/**
	 * Inject Minecraft builtin registry entries into Bukkit API.
	 * 
	 * @see {@link org.bukkit.craftbukkit.CraftRegistry}
	 * @see {@link io.papermc.paper.registry.PaperRegistries}
	 */
	private static boolean injected;

	public static void inject_into_bukkit_registry(MinecraftServer server) {
		// Adds enum constants, so it must run once per JVM; singleplayer builds a new CraftServer per world.
		if (injected) return;
		injected = true;
		register_potions();
		register_entities();
	}
	
	public static EntityType getCraftTypeFromMinecraft(net.minecraft.world.entity.EntityType<?> mc) {
		return MODDED_ENTITIES_MAP.get(mc);
	}
	
	private static void register_entities() {
		DefaultedRegistry<net.minecraft.world.entity.EntityType<?>> registry = BuiltInRegistries.ENTITY_TYPE;

        for (net.minecraft.world.entity.EntityType<?> entity : registry) {
            Identifier id = registry.getKey(entity);
            NamespacedKey key = CraftNamespacedKey.fromMinecraft(id);
            String entityType = normalizeName(id.toString());
			if (org.bukkit.Registry.ENTITY_TYPE.get(key) == null) {
                int typeId = entityType.hashCode();
                
                EntityType bukkitType = EnumHelper.addEnum(
                		EntityType.class,
                		entityType,
                		List.of(String.class, Class.class, Integer.TYPE, Boolean.TYPE),
                		List.of(entityType.toLowerCase(), Entity.class, typeId, false)
                );
                
                BukkitEntityTypeBridge cb = (BukkitEntityTypeBridge) (Object) bukkitType;
                
                cb.cardboard$setKey(key);
                cb.cardboard$addToMaps(entityType.toLowerCase(), (short) typeId);
                MODDED_ENTITIES_MAP.put(entity, bukkitType);

                CardboardMod.LOGGER.info("Registered modded \"" + id + "\" as CraftEntity " + bukkitType);
            }
        }
    }
	
	public static String normalizeName(String name) {
        return name.replace(':', '_')
                .replaceAll("\\s+", "_")
                .replaceAll("\\W", "")
                .toUpperCase(Locale.ENGLISH);
    }

	private static void register_potions() {
		List<PotionType> newTypes = new ArrayList<>();

		for (var potion : BuiltInRegistries.POTION) {
			Identifier location = BuiltInRegistries.POTION.getKey(potion);
			String name = normalizeName(location.toString());
			try {
				PotionType.valueOf(name);
				CardboardMod.LOGGER.info("FOUND POT for " + name);
			} catch (Exception e) {
				NamespacedKey namespacedKey = CraftNamespacedKey.fromMinecraft(location);
				
				PotionType potionType = EnumHelper.addEnum(
						PotionType.class,
						name,
						List.of(String.class),
						List.of( namespacedKey.getKey() )
					);
				newTypes.add(potionType);
				if (CardboardConfig.DEBUG_VERBOSE_CALLS) {
					CardboardMod.LOGGER.info("Registered " + location + " as potion type " + potionType);
				}
			}
		}
	}
}
