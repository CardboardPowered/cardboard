/**
 * The Bukkit for Fabric Project
 * Copyright (C) 2020 Javazilla Software and contributors
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

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.plugin.java.JavaPluginLoader;

public class ReflectionRemapper {

    private static final String NMS_VERSION = "v1_16_R2";

    public static Class<?> getClassForName(String className) throws ClassNotFoundException {
        try {
            return JavaPluginLoader.getByName(className, false);
        } catch (ClassNotFoundException e) {
            return CraftServer.INSTANCE.getClass().getClassLoader().loadClass(className);
            //throw e;
        }
    }

    public static Class<?> getClassByName(Class<?> calling, String className) throws ClassNotFoundException {
        if (className.startsWith("org.bukkit")) {
            return Class.forName(className);
        }
        System.out.println(calling.getName() + " / " + className);
        return JavaPluginLoader.getByName(className, false);
    }

    public static String getPackageName(Package pkage) {
        String name = pkage.getName();
        if (name.startsWith("org.bukkit.craftbukkit"))
            name = name.replace("org.bukkit.craftbukkit", "org.bukkit.craftbukkit." + NMS_VERSION);
        return name;
    }

}