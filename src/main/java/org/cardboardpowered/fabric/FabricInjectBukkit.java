package org.cardboardpowered.fabric;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableList;
import io.izzel.arclight.api.EnumHelper;
import io.izzel.arclight.api.Unsafe;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.VillagerProfession;
import org.bukkit.GameEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.entity.SpawnCategory;
import org.bukkit.entity.Villager;
import org.cardboardpowered.impl.util.CardboardSpawnCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A class that can inject modded things to Bukkit
 */
public class FabricInjectBukkit {

    private static final Logger LOGGER =
            LoggerFactory.getLogger("BukkitRegistry");

    public static final BiMap<Fluid, org.bukkit.Fluid> FLUIDTYPE_FLUID =
            Unsafe.getStatic(CraftMagicNumbers.class, "FLUIDTYPE_FLUID");

    public static void registerAll() {
        loadGameEvents();
        loadFluids();
        loadSpawnCategory();
        loadVillagerProfessions();
    }

    private static void loadGameEvents() {
        try {
            var constructor = GameEvent.class.getDeclaredConstructor(NamespacedKey.class);
            constructor.setAccessible(true);
            var handle = Unsafe.lookup().unreflectConstructor(constructor);
            for (var gameEvent : Registry.GAME_EVENT) {
                var key = Registry.GAME_EVENT.getKey(gameEvent).get().getRegistry();
                if (!isMINECRAFT(key)) {
                    var bukkit = GameEvent.getByKey(CraftNamespacedKey.fromMinecraft(key));
                    if (bukkit == null) {
                        bukkit = (GameEvent) handle.invoke(CraftNamespacedKey.fromMinecraft(key));
                    }
                    LOGGER.debug("Registered {} as game event {}", key, bukkit);
                }
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private static void loadFluids() {
        var id = org.bukkit.Fluid.values().length;
        var newTypes = new ArrayList<org.bukkit.Fluid>();
        Field keyField = Arrays.stream(org.bukkit.Fluid.class.getDeclaredFields()).filter(it -> it.getName().equals("key")).findAny().orElse(null);
        long keyOffset = Unsafe.objectFieldOffset(keyField);
        for (var fluidType : Registry.FLUID) {
            if (!FLUIDTYPE_FLUID.containsKey(fluidType)) {
                var key = Registry.FLUID.getKey(fluidType).get().getRegistry();
                var name = normalizeName(key.toString());
                var bukkit = EnumHelper.makeEnum(org.bukkit.Fluid.class, name, id++, List.of(), List.of());
                Unsafe.putObject(bukkit, keyOffset, CraftNamespacedKey.fromMinecraft(key));
                newTypes.add(bukkit);
                FLUIDTYPE_FLUID.put(fluidType, bukkit);
                LOGGER.debug("Registered {} as fluid {}", key, bukkit);
            }
        }
        EnumHelper.addEnums(org.bukkit.Fluid.class, newTypes);
    }

    private static void loadSpawnCategory() {
        var id = SpawnCategory.values().length;
        var newTypes = new ArrayList<SpawnCategory>();
        for (var category : SpawnGroup.values()) {
            try {
                CardboardSpawnCategory.toBukkit(category);
            } catch (Exception e) {
                var name = category.name();
                var spawnCategory = EnumHelper.makeEnum(SpawnCategory.class, name, id++, List.of(), List.of());
                newTypes.add(spawnCategory);
                LOGGER.debug("Registered {} as spawn category {}", name, spawnCategory);
            }
        }
        EnumHelper.addEnums(SpawnCategory.class, newTypes);
    }

    private static void loadVillagerProfessions() {
        int i = Villager.Profession.values().length;
        List<Villager.Profession> newTypes = new ArrayList<>();
        Field key = Arrays.stream(Villager.Profession.class.getDeclaredFields()).filter(it -> it.getName().equals("key")).findAny().orElse(null);
        long keyOffset = Unsafe.objectFieldOffset(key);
        for (VillagerProfession villagerProfession : Registry.VILLAGER_PROFESSION) {
            var location = Registry.VILLAGER_PROFESSION.getKey(villagerProfession).get().getRegistry();
            if (!isMINECRAFT(location)) {
                String name = normalizeName(location.toString());
                Villager.Profession profession;
                try {
                    profession = Villager.Profession.valueOf(name);
                } catch (Throwable t) {
                    profession = null;
                }
                if (profession == null) {
                    profession = EnumHelper.makeEnum(Villager.Profession.class, name, i++, ImmutableList.of(), ImmutableList.of());
                    newTypes.add(profession);
                    Unsafe.putObject(profession, keyOffset, CraftNamespacedKey.fromMinecraft(location));
                    LOGGER.debug("Registered {} as villager profession {}", location, profession);
                }
            }
        }
        EnumHelper.addEnums(Villager.Profession.class, newTypes);
    }

    public static String normalizeName(String name) {
        return name.toUpperCase(java.util.Locale.ENGLISH).replaceAll("(:|\\s)", "_")
                .replaceAll("\\W", "");
    }

    public static boolean isMINECRAFT(Identifier resourceLocation) {
        return resourceLocation.getNamespace().equals(NamespacedKey.MINECRAFT);
    }
}
