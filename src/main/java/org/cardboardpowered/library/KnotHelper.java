package org.cardboardpowered.library;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;

public class KnotHelper {

    public static boolean PAPER_API_LOADED = false;
    private static final Logger logger = LogManager.getLogger("KnotHelper");

    public static void outdated_fabric_warning(double ver) {
    	logger.error("======== ERROR: FABRIC OUTDATED ========");
        logger.error("| Your version of Fabric is outdated!!");
        logger.error("| You version is: " + ver);
        logger.error("| Lowest Required: 0.13 or higher");
        logger.error("| Update at: https://fabricmc.dev/use/");
        logger.error("=======================================");
    }

    /**
     * Add to class path in Fabric 0.11
     */
    public static void fabric_0_11_load(File file) {
    	try {
            Class<?> l = Class.forName("net.fabricmc.loader.launch.knot.Knot");
            Method m = l.getMethod("getLauncher");
            Object lb = m.invoke(null, null);
            Method m2 = lb.getClass().getMethod("propose", URL.class);
            m2.invoke(lb, file.toURI().toURL());
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("ERROR: Got " + e.getClass().getSimpleName() + " while accessing Fabric Loader.");
        }
    }
    
    /**
     * Add to class path in Fabric 0.12 or higher
     */
    public static void fabric_modern_load(File file) {
    	
        try {
            Class<?> l = Class.forName("net.fabricmc.loader.impl.launch.FabricLauncherBase");
            Field instance = l.getDeclaredField("launcher");
            instance.setAccessible(true);
            Object lb = instance.get(null);
            Class<?> lbc = lb.getClass();
            Method m = lbc.getMethod("addToClassPath", Path.class, String[].class);

            if (!FabricLoader.getInstance().isDevelopmentEnvironment())
                m.invoke(lb, file.toPath(), getPackages());
            logger.info("Debug: Loading library " + file.getName());
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("ERROR: Got " + e.getClass().getSimpleName() + " while accessing Fabric Loader.");
        }
    }

    public static void propose(File file) throws MalformedURLException {
        Version loaderVersion = FabricLoader.getInstance().getModContainer("fabricloader").get().getMetadata().getVersion();
        String verString = loaderVersion.getFriendlyString();
        verString = verString.substring(0, verString.lastIndexOf('.'));
        double ver = Double.valueOf( verString );

        propose_file(file, ver);

        if (file.getName().contains("paper")) {
            PAPER_API_LOADED = true;
        }
    }
    
    public static void propose_file(File file, double ver) {
    	 if (ver < 0.11) {
         	 outdated_fabric_warning(ver);
             return;
         }

         if (ver < 0.12) {
        	 outdated_fabric_warning(ver);
        	 fabric_0_11_load(file);
        	 return;
         }

         if (ver >= 0.12) {
         	fabric_modern_load(file);
         }
    }

    /**
     * Allowed packages.
     * Prevents loading class errors.
     */
    private static String[] getPackages() {
        String[] args = 
        {"co.aikar.timings",
            "co.aikar.util",
            "com.destroystokyo.paper",
            "com.destroystokyo.paper.block",
            "com.destroystokyo.paper.entity",
            "com.destroystokyo.paper.entity.ai",
            "com.destroystokyo.paper.entity.villager",
            "com.destroystokyo.paper.event.block",
            "com.destroystokyo.paper.event.entity",
            "com.destroystokyo.paper.event.executor",
            "com.destroystokyo.paper.event.executor.asm",
            "com.destroystokyo.paper.event.inventory",
            "com.destroystokyo.paper.event.player",
            "com.destroystokyo.paper.event.profile",
            "com.destroystokyo.paper.event.server",
            "com.destroystokyo.paper.exception",
            "com.destroystokyo.paper.inventory",
            "com.destroystokyo.paper.inventory.meta",
            "com.destroystokyo.paper.loottable",
            "com.destroystokyo.paper.network",
            "com.destroystokyo.paper.profile",
            "com.destroystokyo.paper.util",
            "com.destroystokyo.paper.utils",
            "io.papermc.paper.advancement",
            "io.papermc.paper.chat",
            "io.papermc.paper.command",
            "io.papermc.paper.datapack",
            "io.papermc.paper.enchantments",
            "io.papermc.paper.event.block",
            "io.papermc.paper.event.entity",
            "io.papermc.paper.event.packet",
            "io.papermc.paper.event.player",
            "io.papermc.paper.event.server",
            "io.papermc.paper.event.world",
            "io.papermc.paper.event.world.border",
            "io.papermc.paper.inventory",
            "io.papermc.paper.tag",
            "io.papermc.paper.text",
            "io.papermc.paper.util",
            "io.papermc.paper.world",
            "io.papermc.paper.world.generation",
            "io.papermc.paper.entity",
            "io.papermc.paper",
            "org.bukkit",
            "org.bukkit.advancement",
            "org.bukkit.attribute",
            "org.bukkit.block",
            "org.bukkit.block.banner",
            "org.bukkit.block.data",
            "org.bukkit.block.data.type",
            "org.bukkit.block.structure",
            "org.bukkit.boss",
            "org.bukkit.command",
            "org.bukkit.command.defaults",
            "org.bukkit.configuration",
            "org.bukkit.configuration.file",
            "org.bukkit.configuration.serialization",
            "org.bukkit.conversations",
            "org.bukkit.enchantments",
            "org.bukkit.entity",
            "org.bukkit.entity.memory",
            "org.bukkit.entity.minecart",
            "org.bukkit.event",
            "org.bukkit.event.block",
            "org.bukkit.event.command",
            "org.bukkit.event.enchantment",
            "org.bukkit.event.entity",
            "org.bukkit.event.hanging",
            "org.bukkit.event.inventory",
            "org.bukkit.event.player",
            "org.bukkit.event.raid",
            "org.bukkit.event.server",
            "org.bukkit.event.vehicle",
            "org.bukkit.event.weather",
            "org.bukkit.event.world",
            "org.bukkit.generator",
            "org.bukkit.help",
            "org.bukkit.inventory",
            "org.bukkit.inventory.meta",
            "org.bukkit.inventory.meta.tags",
            "org.bukkit.loot",
            "org.bukkit.map",
            "org.bukkit.material",
            "org.bukkit.material.types",
            "org.bukkit.metadata",
            "org.bukkit.permissions",
            "org.bukkit.persistence",
            "org.bukkit.plugin",
            "org.bukkit.plugin.java",
            "org.bukkit.plugin.messaging",
            "org.bukkit.potion",
            "org.bukkit.projectiles",
            "org.bukkit.scheduler",
            "org.bukkit.scoreboard",
            "org.bukkit.structure",
            "org.bukkit.util",
            "org.bukkit.util.io",
            "org.bukkit.util.noise",
            "org.bukkit.util.permissions",
            "org.spigotmc",
            "org.spigotmc.event.entity",
            "org.spigotmc.event.player",
            "net.md_5",
            "net.kyori",
            "org.bukkit",
            "me.isaiah",
            "org.cardboardpowered",
            "com.",
            "net.",
            "org.",
            "me."
        };
        return args;
    }


}