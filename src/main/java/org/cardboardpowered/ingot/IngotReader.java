/**
 * Project Codename "Ingot"
 * Copyright (C) The Cardboard Project
 * Licensed under GPLv3
 * 
 * @author Isaiah
 * @license GPLv3
 */
package org.cardboardpowered.ingot;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;

import org.cardboardpowered.ingot.srglib.JavaType;

import com.javazilla.bukkitfabric.nms.ReflectionMethodVisitor;
import com.javazilla.bukkitfabric.nms.Remapper;

import net.fabricmc.loader.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;

/**
 * The core of Ingot.
 */
@SuppressWarnings("deprecation")
public class IngotReader {

    public static HashMap<String, String> classes_S2F = new HashMap<>(); // NMS -> Fabric
    public static HashMap<String, String> classes_S2O = new HashMap<>(); // NMS -> Obf
    public static HashMap<String, String> classes_F2O = new HashMap<>(); // Fabric -> Obf

    public static HashMap<String, IngotMethodInfo> methods_S2F = new HashMap<>();
    public static HashMap<String, IngotMethodInfo> methods_S2F_NR = new HashMap<>(); // Same as above but with no return type
    public static HashMap<String, String> fields_S2F = new HashMap<>();

    public static HashMap<String, String> obfFields = new HashMap<>();

    public static boolean finishedSetup;
    public static File outFile;

    /**
     * Get the mapped method name
     * 
     * @return {@link IngotMethodInfo} or null if no mapping is found.
     * @param spigotClass - Spigot NMS class
     * @param spigotMethod - The Spigot name of the method
     * @param params - The descriptor of the method
     * @param isDescriptorMappedToFabric - true if the provided descriptor is mapped to fabric, false if the descriptor is in spigot mappings.
     * @see {@link #getMethodInfo(String, String, String, boolean)}
     */
    public static IngotMethodInfo getMethodInfo(String spigotClass, String spigotMethod, Class<?>[] params, boolean isDescriptorMappedToFabric) {
        String des = "(";
        for (Class<?> cl : params)
            des += JavaType.fromInternalName(cl.getTypeName());
        des += ")";
        return getMethodInfo(spigotClass, spigotMethod, des, isDescriptorMappedToFabric);
    }

    public static String getDescFromArray(Class<?>[] params) {
        String des = "(";
        for (Class<?> cl : params)
            des += JavaType.fromInternalName(cl.getTypeName());
        des += ")";
        return des;
    }

    /**
     * Get the mapped method name
     * 
     * @return {@link IngotMethodInfo} or null if no mapping is found.
     * @param spigotClass - Spigot NMS class
     * @param spigotMethod - The Spigot name of the method
     * @param fabricDescriptor - The descriptor of the method
     * @param isDescriptorMappedToFabric - true if the provided descriptor is mapped to fabric, false if the descriptor is in spigot mappings.
     */
    public static IngotMethodInfo getMethodInfo(String spigotClass, String spigotMethod, String fabricDescriptor, boolean isDescriptorMappedToFabric) {
        String descriptor = fabricDescriptor;
        if (!isDescriptorMappedToFabric) {
            descriptor = IngotMethodInfo.asFabricDescriptor(fabricDescriptor);
        }

        String key = spigotClass.replace('/','.') + "|" + spigotMethod + "|" + descriptor;
        if (methods_S2F.containsKey(key)) {
            return methods_S2F.get(key);
        }
        if (methods_S2F_NR.containsKey(key)) {
            return methods_S2F_NR.get(key);
        }
        return null;
    }

    /**
     * Get the parent class of a fabric class.
     * @return the superclass
     */
    public static Class<?> getParentClass(String fabricClass) throws ClassNotFoundException {
        return Class.forName(fabricClass.replace('/','.')).getSuperclass();
    }

    /**
     * Get the mapped field name
     * 
     * @return The name of the mapped field, or the specified default if no mapping is found.
     * @param spigotClass - Spigot NMS class
     * @param spigotFieldName - The NMS field name
     * @param orDefault - What to return if no mapping is found
     */
    public static String getFieldName(String spigotClass, String spigotFieldName, String orDefault) {
        return fields_S2F.getOrDefault(spigotClass.replace('/','.') + "|" + spigotFieldName, orDefault);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void start() {
        classes_S2F.put("org.bukkit.craftbukkit.CraftWorld", "org.cardboardpowered.impl.world.WorldImpl");
        classes_S2F.put("org.bukkit.craftbukkit.entity.CraftPlayer", "org.cardboardpowered.impl.entity.PlayerImpl");
        //classes_S2F.put("org.bukkit.craftbukkit.entity.CraftHumanEntity", "org.cardboardpowered.impl.entity.HumanEntityImpl");
        classes_S2F.put("org.bukkit.craftbukkit.entity.CraftLivingEntity", "org.cardboardpowered.impl.entity.LivingEntityImpl");
        classes_S2F.put("org.bukkit.craftbukkit.entity.CraftArmorStand", "org.cardboardpowered.impl.entity.ArmorStandImpl");

        Remapper.addProvider(new IngotProvider());
        ReflectionMethodVisitor.SKIP.remove("essentials");

        MappingResolver mr = FabricLoader.INSTANCE.getMappingResolver();
        FabricLoader.INSTANCE.getModsDirectory().getParentFile().mkdirs();
        File fold = new File(FabricLoader.INSTANCE.getModsDirectory().getParentFile(), "bdmappings");
        fold.mkdirs();

        String version = "1.16.4";
        File cl = exportResource("bukkit-" + version + "-cl.csrg", fold);
        File mb = exportResource("bukkit-" + version + "-members.csrg", fold);

        // Because of private access
        try {
            Method m = mr.getClass().getDeclaredMethod("getNamespaceData", String.class);
            m.setAccessible(true);
            Object o = m.invoke(mr, "official");
            Field f = o.getClass().getDeclaredField("fieldNames");
            f.setAccessible(true);
            HashMap map = (HashMap) f.get(o);
            map.forEach((a,b) -> {
                try {
                    Field f1 = a.getClass().getDeclaredField("owner");
                    f1.setAccessible(true);
                    Field f2 = a.getClass().getDeclaredField("name");
                    f2.setAccessible(true);

                    String owner = (String) f1.get(a);
                    String name = (String) f2.get(a);
                    obfFields.put(owner + "|" + name, (String) b);
                    //System.out.println(owner + "|" + name + " = " + b);
                } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
                    e.printStackTrace();
                }
            });
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | NoSuchFieldException e1) {
            e1.printStackTrace();
        }

        classes_S2F.put("net.minecraft.server.MinecraftServer", "net.minecraft.server.MinecraftServer");
        try {
            for (String s : Files.readAllLines(cl.toPath())) {
                classes_S2F.put("net.minecraft.server." + s.split(" ")[1].trim(), mr.mapClassName("official", s.split(" ")[0]));
                classes_S2O.put("net.minecraft.server." + s.split(" ")[1].trim(), s.split(" ")[0]);
                classes_F2O.put(mr.mapClassName("official", s.split(" ")[0]).replace('/','.'), s.split(" ")[0]);
            }

            for (String s : Files.readAllLines(mb.toPath())) {
                if (s.startsWith("#")) continue;
                String[] spl = s.split(" ");
                if (spl.length <= 3) {
                    // Field
                    fields_S2F.put("net.minecraft.server." + spl[0] + "|" + spl[2], obfFields.get(classes_S2O.getOrDefault("net.minecraft.server." + spl[0], spl[0].replace('/','.')) + "|" + spl[1]));
                    continue;
                }
                // Method
                IngotMethodInfo mi = new IngotMethodInfo(s);
                methods_S2F.put(mi.spigotClassName + "|" + mi.spigotMethodName + "|" + mi.fabricDescriptor, mi);
                methods_S2F_NR.put(mi.spigotClassName + "|" + mi.spigotMethodName + "|" + mi.fabricDescriptor.substring(0,mi.fabricDescriptor.indexOf(")")-1), mi);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        File out = new File(fold, "ingot-cl-1.16.4.csrg");
        try {
            out.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String s = "# (C) CardboardPowered.org\n";

        for (String str : classes_S2F.keySet()) {
            String t = str.replace('.', '/') + " " + classes_S2F.get(str).replace('.','/') + "\n";
            if (!t.contains("#") || t.startsWith("# (C) Cardboard")) s += t + "\n";
        }
        try {
            Files.write(out.toPath(), s.getBytes(), StandardOpenOption.SYNC, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        outFile = out;
        finishedSetup = true;
    }

    public static File exportResource(String res, File folder) {
        try (InputStream stream = IngotReader.class.getClassLoader().getResourceAsStream(res)) {
            if (stream == null) throw new IOException("Null " + res);

            File f = new File(folder, res);
            f.createNewFile();
            Files.copy(stream, f.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return f;
        } catch (IOException e) { e.printStackTrace(); return null;}
    }

}