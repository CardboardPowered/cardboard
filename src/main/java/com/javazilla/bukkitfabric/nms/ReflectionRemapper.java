/**
 * Cardboard - Bukkit/Spigot/Paper API for Fabric
 * Copyright (C) 2023, CardboardPowered.org
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
package com.javazilla.bukkitfabric.nms;

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

import net.minecraft.SharedConstants;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerNetworkIo;

/**
 * Very unsafe re-mapping of Reflection.
 */
public class ReflectionRemapper {

    private static final String NMS_VERSION = "v1_20_R1";// "v1_19_R3";
    public static JavaPlugin plugin;

    public static String mapClassName(String className) {
        if (className.startsWith("org.bukkit.craftbukkit." + NMS_VERSION + "."))
            return MappingsReader.getIntermedClass("org.bukkit.craftbukkit." + className.substring(23 + NMS_VERSION.length() + 1));

        if (className.startsWith("org.bukkit.craftbukkit.CraftServer."))
            return MappingsReader.getIntermedClass(className.replace("org.bukkit.craftbukkit.CraftServer.", "org.bukkit.craftbukkit."));

        if (className.startsWith("net.minecraft.server." + NMS_VERSION + "."))
            return MappingsReader.getIntermedClass(className.replace("net.minecraft.server." + NMS_VERSION + ".", "net.minecraft.server."));

        if (className.startsWith("net.minecraft.") && !className.startsWith("class_"))
            return MappingsReader.getIntermedClass(className);

        if (className.startsWith("org.bukkit.craftbukkit."))
            return MappingsReader.getIntermedClass(className); // We are not CraftBukkit, check for our own version of the class.

        if (className.startsWith("net.minecraft.server.CraftServer."))
            return MappingsReader.getIntermedClass(className.replace("net.minecraft.server.CraftServer.", "net.minecraft.server."));

        return className;
    }

    public static Class<?> getClassForName(String className) throws ClassNotFoundException {
        return getClassFromJPL(className);
    }

    public static Field getFieldByName(Class<?> calling, String f) throws ClassNotFoundException {
        try {
            Field field = calling.getDeclaredField(MappingsReader.getIntermedField(calling.getName(), f));
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException | SecurityException e) {
            try {
                Field a = calling.getDeclaredField(MappingsReader.getIntermedField(calling.getName(), f));
                a.setAccessible(true);
                return a;
            } catch (NoSuchFieldException | SecurityException e1) {
                Class<?> whyIsAsmBroken = getClassFromJPL(getCallerClassName());
                try {
                    Field a = whyIsAsmBroken.getDeclaredField(MappingsReader.getIntermedField(whyIsAsmBroken.getName(), f));
                    a.setAccessible(true);
                    return a;
                } catch (NoSuchFieldException | SecurityException e2) {
                    if (f.contains("B_STATS_VERSION")) {
                        return getBstatsVersionField();
                    }
                    //System.out.println("DeBug:" + calling.getName() + " / " + getCallerClassName());
                    e2.printStackTrace();
                }
                return null;
            }
        }
    }

    private static int BV_CALLED = 0;
    public static Field getBstatsVersionField() {
        Field f = null;
        int i = 0;
        for (final Class<?> service : Bukkit.getServicesManager().getKnownServices()) {
            if (i < BV_CALLED) {
                i++;
                continue;
            }
            try {
                f = service.getField("B_STATS_VERSION"); // Identifies bStats classes
                break;
            } catch (final NoSuchFieldException ignored) {
            }
        }
        BV_CALLED++;
        return f;
    }

    public static Field getDeclaredFieldByName(Class<?> calling, String f) throws ClassNotFoundException, NoSuchFieldException {
        try {
            return calling.getDeclaredField(MappingsReader.getIntermedField(calling.getName(), f));
        } catch (NoSuchFieldException | SecurityException e) {
            try {
                Field a = calling.getDeclaredField(MappingsReader.getIntermedField(calling.getName(), f));
                a.setAccessible(true);
                return a;
            } catch (NoSuchFieldException | SecurityException e1) {
                Class<?> whyIsAsmBroken = getClassFromJPL(getCallerClassName());
                try {
                    if (f.contains("connectedChannels")) {
                        Field a = ServerNetworkIo.class.getDeclaredField("connections");
                        a.setAccessible(true);
                        return a;
                    }
                    if (null != whyIsAsmBroken && whyIsAsmBroken.getName().equals("protocolsupport.zplatform.impl.spigot.injector.network.SpigotNettyInjector") && f.contains("f")) {
                        Field a = ServerNetworkIo.class.getDeclaredField("channels");
                        a.setAccessible(true);
                        return a;
                    }
                    if (null == whyIsAsmBroken) {
                        System.out.println("CALLING: " + calling.getName() + ", F: " + f);
                        return null;
                    }
                    Field a = whyIsAsmBroken.getDeclaredField(MappingsReader.getIntermedField(whyIsAsmBroken.getName(), f));
                    a.setAccessible(true);
                    return a;
                } catch (NoSuchFieldException | SecurityException e2) {
                    throw e2;
                    //e1.printStackTrace();
                }
               // return null;
            }
        }
    }

    public static Method getMethodByName(Class<?> calling, String f) throws ClassNotFoundException, NoSuchMethodException {
        Method m = getDeclaredMethodByName(calling, f);
        m.setAccessible(true);
        return m;
    }
    
    public static CraftServer getCraftServer() {
        return CraftServer.INSTANCE;
    }

    public static MinecraftServer getNmsServer() {
        return CraftServer.server;
    }

    public static Method[] getMethods(Class<?> calling) {
        Method[] r = calling.getMethods();
        if (calling.getSimpleName().contains("MinecraftServer")) {
            Method[] nr = new Method[r.length+1];
            for (int i = 0; i < r.length; i++) {
                nr[i] = r[i];
            }
            try {
                nr[r.length] = ReflectionRemapper.class.getMethod("getNmsServer");
            } catch (NoSuchMethodException | SecurityException e) {
                e.printStackTrace();
            }
            return nr;
        }
        return r;
    }

    @Deprecated
    public static Method getDeclaredMethodByName(Class<?> calling, String f) throws ClassNotFoundException, NoSuchMethodException {
        if (calling.getName().endsWith("MinecraftServer") && f.equalsIgnoreCase("getServer")) {
            return BukkitFabricMod.GET_SERVER;
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
                    throw e2;
                    //e1.printStackTrace();
                }
                //return null;
            }
        }
    }

    @Deprecated
    public static Method getDeclaredMethodByName(Class<?> calling, String f, Class<?>[] parms) throws ClassNotFoundException, NoSuchMethodException {
        if (calling.getName().endsWith("MinecraftServer") && f.equalsIgnoreCase("getServer")) {
            return BukkitFabricMod.GET_SERVER;
        }
            
        try {
            return calling.getMethod(MappingsReader.getIntermedMethod(calling.getName(), f, parms), parms);
        } catch (NoSuchMethodException | SecurityException e) {
            try {
                Method a = calling.getDeclaredMethod(MappingsReader.getIntermedMethod(calling.getName(), f, parms), parms);
                a.setAccessible(true);
                return a;
            } catch (NoSuchMethodException | SecurityException e1) {
                Class<?> whyIsAsmBroken = getClassFromJPL(getCallerClassName());
                try {
                    Method a = whyIsAsmBroken.getDeclaredMethod(MappingsReader.getIntermedMethod(whyIsAsmBroken.getName(), f), parms);
                    a.setAccessible(true);
                    return a;
                } catch (NoSuchMethodException | SecurityException e2) {
                    e1.printStackTrace();
                }
                return getDeclaredMethodByName(calling, f);
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
            BukkitFabricMod.LOGGER.warning("SOMETHING EVERY WRONG! PLEASE REPORT THE EXCEPTION BELOW TO CARDBOARD:");
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
    public static String getPackageName(Package pkage) {
        String name = pkage.getName();
        if (name.startsWith("org.bukkit.craftbukkit"))
            name = name.replace("org.bukkit.craftbukkit", "org.bukkit.craftbukkit." + NMS_VERSION);
        return name;
    }

    /**
     */
    public static String getClassName(Class<?> clazz) {
        String name = clazz.getName();
        if (name.startsWith("org.bukkit.craftbukkit"))
            name = name.replace("org.bukkit.craftbukkit", "org.bukkit.craftbukkit." + NMS_VERSION);
        return name;
    }

    /**
     */
    public static String getCanonicalName(Class<?> clazz) {
        String name = clazz.getName();
        if (name.startsWith("org.bukkit.craftbukkit"))
            name = name.replace("org.bukkit.craftbukkit", "org.bukkit.craftbukkit." + NMS_VERSION);
        return name;
    }

    /**
     */
    public static String getMinecraftServerVersion() {
        return SharedConstants.getGameVersion().getName();
    }

    public static Method getMethodByName(Class<?> calling, String f, Class<?>[] p) throws ClassNotFoundException, NoSuchMethodException {
        Method m = getDeclaredMethodByName(calling, f, p);
        m.setAccessible(true);
        return m;
    }

}