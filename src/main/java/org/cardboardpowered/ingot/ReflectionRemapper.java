/**
 * Cardboard - Bukkit/Spigot/Paper API for Fabric
 * Copyright (C) 2020, CardboardPowered.org
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.cardboardpowered.ingot;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import com.javazilla.bukkitfabric.BukkitFabricMod;
import com.javazilla.bukkitfabric.nms.MappingsReader;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.server.MinecraftServer;

/**
 * Very unsafe re-mapping of Reflection.
 */
public class ReflectionRemapper {

    private static final String[] SUPPORTED_NMS_VERSIONS = {"v1_16_R3", "v1_16_R2"};
    public static JavaPlugin plugin;

    public static String mapClassName(String className) {
        if (className.startsWith("?")) {
            // BKCommonLib why?
            System.out.println("BF CARDBOARD FOUND QUESTION MARK ON CLASS NAME: " + className);
            className = className.replace("?", "");
        }

        for (String NMS_VERSION : SUPPORTED_NMS_VERSIONS)
            if (className.startsWith("org.bukkit.craftbukkit." + NMS_VERSION + "."))
                return MappingsReader.getIntermedClass("org.bukkit.craftbukkit." + className.substring(23 + NMS_VERSION.length() + 1));

        if (className.startsWith("org.bukkit.craftbukkit.CraftServer."))
            return MappingsReader.getIntermedClass(className.replace("org.bukkit.craftbukkit.CraftServer.", "org.bukkit.craftbukkit."));

        for (String NMS_VERSION : SUPPORTED_NMS_VERSIONS)
            if (className.startsWith("net.minecraft.server." + NMS_VERSION + "."))
                return IngotReader.classes_S2F.getOrDefault(className.replace("net.minecraft.server." + NMS_VERSION + ".", "net.minecraft.server."), className);

        if (className.startsWith("org.bukkit.craftbukkit."))
            return MappingsReader.getIntermedClass(className); // We are not CraftBukkit, check for our own version of the class.

        if (className.startsWith("net.minecraft.server.CraftServer."))
            return IngotReader.classes_S2F.getOrDefault(className.replace("net.minecraft.server.CraftServer.", "net.minecraft.server."), className);

        if (className.startsWith("net.minecraft.server"))
            return IngotReader.classes_S2F.getOrDefault(className, className);

        return className;
    }

    public static String getIntermedField(String clazz, String name) throws NoSuchFieldException, SecurityException, ClassNotFoundException {
        String s = IngotReader.getFieldName(clazz, name, "<BF_ERROR_GETTING_FIELD>");
        if (s.equalsIgnoreCase("<BF_ERROR_GETTING_FIELD>")) {
            String fc = IngotReader.classes_S2O.getOrDefault(clazz.replace('/','.'), clazz);
            fc = IngotReader.classes_F2O.getOrDefault(fc, fc);
            String fo = IngotReader.obfFields.getOrDefault(fc + "|" + name, name);
            return fo;
        }
        return s;
    }

    public static String getIntermedMethod(String clazz, String m, Class<?>[] parms) {
        IngotMethodInfo mi = IngotReader.getMethodInfo(clazz, m, parms, true);
        if (null != mi)
            return mi.fabricMethodName;
        String fc = IngotReader.classes_S2F.getOrDefault(clazz.replace('/','.'), clazz);
        String mo = FabricLoader.getInstance().getMappingResolver().mapMethodName("official", fc, m, IngotReader.getDescFromArray(parms));
        return mo;
    }

    public static Class<?> getClassForName(String className) throws ClassNotFoundException {
        return getClassFromJPL(className);
    }

    public static Field getFieldByName(Class<?> calling, String f) throws ClassNotFoundException {
        try {
            Field field = calling.getDeclaredField(getIntermedField(calling.getName(), f));
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException | SecurityException e) {
            try {
                Field a = calling.getDeclaredField(getIntermedField(calling.getName(), f));
                a.setAccessible(true);
                return a;
            } catch (NoSuchFieldException | SecurityException e1) {
                Class<?> whyIsAsmBroken = getClassFromJPL(getCallerClassName());
                try {
                    Field a = whyIsAsmBroken.getDeclaredField(getIntermedField(whyIsAsmBroken.getName(), f));
                    a.setAccessible(true);
                    return a;
                } catch (NoSuchFieldException | SecurityException e2) {
                    e2.printStackTrace();
                }
                return null;
            }
        }
    }

    public static Field getDeclaredFieldByName(Class<?> calling, String f) throws ClassNotFoundException {
        try {
            return calling.getDeclaredField(getIntermedField(calling.getName(), f));
        } catch (NoSuchFieldException | SecurityException e) {
            try {
                Field a = calling.getDeclaredField(getIntermedField(calling.getName(), f));
                a.setAccessible(true);
                return a;
            } catch (NoSuchFieldException | SecurityException e1) {
                Class<?> whyIsAsmBroken = getClassFromJPL(getCallerClassName());
                try {
                    Field a = whyIsAsmBroken.getDeclaredField(getIntermedField(whyIsAsmBroken.getName(), f));
                    a.setAccessible(true);
                    return a;
                } catch (NoSuchFieldException | SecurityException e2) {
                    e1.printStackTrace();
                }
                return null;
            }
        }
    }

    public static Method getMethodByName1(Class<?> calling, String f) throws ClassNotFoundException {
        Method m = getDeclaredMethodByName1(calling, f);
        m.setAccessible(true);
        return m;
    }

    public static Method getMethodByName(Class<?> calling, String f, Class<?>[] p) throws ClassNotFoundException {
        Method m = getDeclaredMethodByName(calling, f, p);
        m.setAccessible(true);
        return m;
    }

    public static MinecraftServer getNmsServer() {
        return CraftServer.server;
    }

    @Deprecated
    public static Method getDeclaredMethodByName1(Class<?> calling, String f) throws ClassNotFoundException {
        if (calling.getName().endsWith("MinecraftServer") && f.equalsIgnoreCase("getServer")) {
            try {
                return ReflectionRemapper.class.getMethod("getNmsServer");
            } catch (NoSuchMethodException | SecurityException e) {
                e.printStackTrace();
            }
        }
            
        try {
            return calling.getMethod(MappingsReader.getIntermedMethod(calling.getName(), f));
        } catch (NoSuchMethodException | SecurityException e) {
            try {
                Method a = calling.getDeclaredMethod(MappingsReader.getIntermedMethod(calling.getName(), f));
                a.setAccessible(true);
                return a;
            } catch (NoSuchMethodException | SecurityException e1) {
                Class<?> whyIsAsmBroken = getClassFromJPL(getCallerClassName());
                try {
                    Method a = whyIsAsmBroken.getDeclaredMethod(MappingsReader.getIntermedMethod(whyIsAsmBroken.getName(), f));
                    a.setAccessible(true);
                    return a;
                } catch (NoSuchMethodException | SecurityException e2) {
                    e1.printStackTrace();
                }
                return null;
            }
        }
    }

    //public static void main(String[] args) throws ClassNotFoundException {
    //    Class[] test = {int.class};
    //    getDeclaredMethodByName(null, "", test);
    //}

    public static Method getDeclaredMethodByName(Class<?> calling, String f, Class<?>[] parms) throws ClassNotFoundException {
        if (calling.getName().endsWith("MinecraftServer") && f.equalsIgnoreCase("getServer")) {
            try {
                return ReflectionRemapper.class.getMethod("getNmsServer");
            } catch (NoSuchMethodException | SecurityException e) {
                e.printStackTrace();
            }
        }
            
        try {
            return calling.getMethod(getIntermedMethod(calling.getName(), f, parms), parms);
        } catch (NoSuchMethodException | SecurityException e) {
            try {
                Method a = calling.getDeclaredMethod(getIntermedMethod(calling.getName(), f, parms), parms);
                a.setAccessible(true);
                return a;
            } catch (NoSuchMethodException | SecurityException e1) {
                Class<?> whyIsAsmBroken = getClassFromJPL(getCallerClassName());
                try {
                    Method a = whyIsAsmBroken.getDeclaredMethod(getIntermedMethod(whyIsAsmBroken.getName(), f, parms), parms);
                    a.setAccessible(true);
                    return a;
                } catch (NoSuchMethodException | SecurityException e2) {
                    e1.printStackTrace();
                }
                return getDeclaredMethodByName1(calling, f);
            }
        }
    }

    /**
     * Retrieve a class that is from a plugin
     * 
     * @author Isaiah
     */
    @SuppressWarnings("unchecked")
    public static Class<?> getClassFromJPL(String name) {
        try {
            SimplePluginManager pm = (SimplePluginManager) Bukkit.getPluginManager();
            Field fa = SimplePluginManager.class.getDeclaredField("fileAssociations");
            fa.setAccessible(true);
            Map<Pattern, PluginLoader> pl = (Map<Pattern, PluginLoader>) fa.get(pm);
            JavaPluginLoader jpl = null;
            for (PluginLoader loader : pl.values()) {
                if (loader instanceof JavaPluginLoader) {
                    jpl = (JavaPluginLoader) loader;
                    break;
                }
            }

            Method fc = JavaPluginLoader.class.getDeclaredMethod("getClassByName", String.class);
            fc.setAccessible(true);
            return (Class<?>) fc.invoke(jpl, name);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            BukkitFabricMod.LOGGER.warning("SOMETHING EVERY WRONG! PLEASE REPORT THE EXCEPTION BELOW TO BUKKIT4FABRIC:");
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static JavaPluginLoader getFirstJPL() {
        try {
            SimplePluginManager pm = (SimplePluginManager) Bukkit.getPluginManager();
            if (null == pm) System.out.println(" NULL PM ");
            Field fa = SimplePluginManager.class.getDeclaredField("fileAssociations");
            fa.setAccessible(true);
            Map<Pattern, PluginLoader> pl = (Map<Pattern, PluginLoader>) fa.get(pm);
            JavaPluginLoader jpl = null;
            for (PluginLoader loader : pl.values()) {
                if (loader instanceof JavaPluginLoader) {
                    jpl = (JavaPluginLoader) loader;
                    break;
                }
            }
            return jpl;
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            BukkitFabricMod.LOGGER.warning("SOMETHING EVERY WRONG! PLEASE REPORT THE EXCEPTION BELOW TO BUKKIT4FABRIC:");
            e.printStackTrace();
            return null;
        }
    }

    /**
     */
    public static String getCallerClassName() { 
        StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
        for (int i=1; i<stElements.length; i++) {
            StackTraceElement ste = stElements[i];
            if (!ste.getClassName().equals(ReflectionRemapper.class.getName()) && ste.getClassName().indexOf("java.lang.Thread")!=0)
                return ste.getClassName();
        }
        return null;
    }

    /**
     */
    public static String getMinecraftServerVersion() {
        return SharedConstants.getGameVersion().getName();
    }

}