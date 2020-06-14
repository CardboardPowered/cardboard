package com.fungus_soft.bukkitfabric;

import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

public class BukkitLogger extends Logger {

    private org.apache.logging.log4j.Logger log4j;
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
        else
            log4j.log(convertLevel(lr.getLevel()), lr.getMessage(), lr.getThrown());
    }

    private Level convertLevel(java.util.logging.Level l) {
        if (l == java.util.logging.Level.ALL)
            return Level.ALL;
        if (l == java.util.logging.Level.CONFIG)
            return Level.TRACE;
        if (l == java.util.logging.Level.WARNING)
            return Level.WARN;
        if (l == java.util.logging.Level.INFO)
            return Level.INFO;
        if (l == java.util.logging.Level.OFF)
            return Level.OFF;
        if (l == java.util.logging.Level.SEVERE)
            return Level.FATAL;
        if (l == java.util.logging.Level.FINE || l == java.util.logging.Level.FINER || l == java.util.logging.Level.FINEST)
            return Level.WARN;
        return Level.ALL;
    }

}