package com.fungus_soft.bukkitfabric.nms;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import com.fungus_soft.bukkitfabric.BukkitLogger;

import net.fabricmc.loader.api.FabricLoader;
import net.md_5.specialsource.SpecialSource;

public class Remapper {

    public static BukkitLogger LOGGER = new BukkitLogger("BukkitNmsRemapper", null);

    public static File configDir = new File(FabricLoader.getInstance().getConfigDirectory(), "bukkit4fabric");
    public static File remappedDir = new File(configDir, "remapped-plugins");

    public static File versionFix, spigot2inter, inter2obf;

    /**
     * Remaps NMS used in plugins<br><br>
     * 
     * 1. Maps Spigot-NMS to Fabric intermediary<br>
     * 2. Maps intermediary to obf.<br><br>
     * 
     * These steps will hopefully allow plugins to use NMS during snapshots
     */
    public static void remap(File jarFile) {
        if (versionFix == null) {
            configDir.mkdirs();
            remappedDir.mkdirs();
            versionFix = new File(configDir, "deversionify-spigot.srg");
            spigot2inter = new File(configDir, "spigot2intermediary.csrg");
            inter2obf = new File(configDir, "intermediary2obf.csrg");
    
            // Export Mappings to File
            exportResource("deversionify-spigot.srg", configDir);
            exportResource("spigot2intermediary.csrg", configDir);
            exportResource("intermediary2obf.csrg", configDir);
        }

        String jarName = jarFile.getName().substring(0, jarFile.getName().indexOf(".jar"));
        LOGGER.info("Remapping \"" + jarFile + "\"...");

        // net.minecraft.server.v1_XX_R1 -> net.minecraft.server
        File deversionify = new File(remappedDir, jarName + "-deversionify.jar");
        runSpecialSource(versionFix, jarFile, deversionify);

        // Spigot -> Intermediary
        File intermediary = new File(remappedDir, jarName + "-intermediary.jar");
        runSpecialSource(spigot2inter, deversionify, intermediary);

        // Intermediary -> Obf
        File finalJar = new File(remappedDir, jarName + "-obf.jar");
        runSpecialSource(inter2obf, intermediary, finalJar);

        // Cleanup
        deversionify.delete();
        intermediary.delete();

        try {
            Files.move(finalJar.toPath(), jarFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
        finalJar.delete();
    }

    public static void runSpecialSource(File mappingsFile, File inJar, File outJar) {
        String[] args = {"-q", "-i", inJar.getAbsolutePath(), "-o", outJar.getAbsolutePath(), "-m", mappingsFile.getAbsolutePath()};
        try {
            SpecialSource.main(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Path exportResource(String res, File folder) {
        try (InputStream stream = Remapper.class.getClassLoader().getResourceAsStream("mappings/" + res)) {
            if (stream == null) throw new IOException("Null " + res);

            File f = new File(folder, res);
            f.createNewFile();
            Files.copy(stream, f.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return f.toPath();
        } catch (IOException e) { e.printStackTrace(); return null;}
    }

}