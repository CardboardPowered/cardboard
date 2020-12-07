/**
 * CardboardPowered - Bukkit/Spigot for Fabric
 * Copyright (C) CardboardPowered.org and contributors
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either 
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.javazilla.bukkitfabric;

import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

public class BukkitLogger extends Logger {

    public org.apache.logging.log4j.Logger log4j;
    private static BukkitLogger inst;

    public static BukkitLogger getLogger() {
        if (inst == null)
            new BukkitLogger();
        return inst;
    }

    public BukkitLogger() {
        this("Bukkit", null);
    }

    public BukkitLogger(String name, String str) {
        super(name, str);
        this.log4j = LogManager.getLogger(name);
        if (inst == null)
            inst = this;
    }

    @Override
    public void log(LogRecord lr) {
        if (lr.getThrown() == null)
            log4j.log(convertLevel(lr.getLevel()), lr.getMessage());
        else log4j.log(convertLevel(lr.getLevel()), lr.getMessage(), lr.getThrown());
    }

    private Level convertLevel(java.util.logging.Level l) {
        if (l == java.util.logging.Level.ALL)     return Level.ALL;
        if (l == java.util.logging.Level.CONFIG)  return Level.TRACE;
        if (l == java.util.logging.Level.WARNING) return Level.WARN;
        if (l == java.util.logging.Level.INFO)    return Level.INFO;
        if (l == java.util.logging.Level.OFF)     return Level.OFF;
        if (l == java.util.logging.Level.SEVERE)  return Level.FATAL;

        if (l == java.util.logging.Level.FINE || l == java.util.logging.Level.FINER || l == java.util.logging.Level.FINEST)
            return Level.WARN;
        return Level.ALL;
    }

}