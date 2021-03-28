package com.javazilla.bukkitfabric.nms;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.cardboardpowered.ingot.IngotReader;

import com.javazilla.bukkitfabric.BukkitLogger;

import net.fabricmc.loader.api.FabricLoader;
import net.md_5.specialsource.SpecialSource;

/**
 * @deprecated To be replaced with our Ingot remapping tool
 */
@Deprecated
public class Remapper {

    public static List<Provider> providers = new ArrayList<>();
    public static void addProvider(Provider provider) {
        providers.add(provider);
    }

    public static int MAPPINGS_VERSION = 24;

    public static BukkitLogger LOGGER = new BukkitLogger("BukkitNmsRemapper", null);

    public static File configDir = new File(FabricLoader.getInstance().getConfigDir().toFile(), "bukkit4fabric");
    public static File remappedDir = new File(configDir, "remapped-plugins");
    public static File backup = new File(remappedDir, "backup-plugins");
    public static File versionFix;
    public static File spigot2inter;
    public static File md5info = new File(remappedDir, "md5-hashes.dat");

    public static List<String> hashes = new ArrayList<>();

    /**
     * Remaps NMS used in plugins<br><br>
     * 
     * 1. Maps Spigot-NMS to Fabric intermediary<br>
     * 2. Maps intermediary to obf.<br><br>
     * 
     * These steps will hopefully allow plugins to use NMS during snapshots
     */
    public static void remap(File jarFile) {
        configDir.mkdirs();
        remappedDir.mkdirs();
        backup.mkdirs();

        for (Provider p : Remapper.providers) {
            boolean b = p.remap(jarFile);
            if (b) return;
        }

        if (versionFix == null) {
            if (md5info.exists()) {
                try {
                    hashes.addAll(Files.readAllLines(md5info.toPath()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                md5info.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            versionFix = new File(configDir, "deversionify-spigot.srg");
            spigot2inter = new File(configDir, "spigot2intermediary.csrg");

            // Export Mappings to File
            exportResource("deversionify-spigot.srg", configDir);
            exportResource("spigot2intermediary.csrg", configDir);
        }
        String md5 = null;
        try (InputStream is = Files.newInputStream(jarFile.toPath())) {
            md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(is);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        File toMap = jarFile;
        boolean usingBackup = false;
        if (hashes.size() <= 0 || !hashes.get(0).equals("mappings=" + MAPPINGS_VERSION)) {
            toMap = new File(backup, jarFile.getName());
            if (!toMap.exists()) {
                toMap = jarFile;
                usingBackup = false;
            } else usingBackup = true;
        }

        if (hashes.contains(md5) && hashes.get(0).equals("mappings=" + MAPPINGS_VERSION)) return;

        String jarName = jarFile.getName().substring(0, jarFile.getName().indexOf(".jar"));
        LOGGER.info("Remapping \"" + jarFile + "\"...");

        // net.minecraft.server.v1_XX_RX -> net.minecraft.server
        File deversionify = new File(remappedDir, jarName + "-deversionify.jar");
        runSpecialSource(versionFix, toMap, deversionify);

        // Spigot -> Intermediary
        File finalJar = new File(remappedDir, jarName + "-intermediary.jar");
        //System.out.println(IngotReader.finishedSetup);
        runSpecialSource(IngotReader.finishedSetup ? IngotReader.outFile : spigot2inter, deversionify, finalJar);

        // Cleanup
        deversionify.delete();

        if (!usingBackup) {
            try {
                Files.copy(jarFile.toPath(), new File(backup, jarFile.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            Files.copy(finalJar.toPath(), jarFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (InputStream is = Files.newInputStream(jarFile.toPath())) {
            md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(is);
        } catch (IOException e1) {
            md5 = null;
            e1.printStackTrace();
        }
        if (null != md5) hashes.add(md5);
        finalJar.delete();
        saveHashes();
    }

    public static void saveHashes() {
        String out = "mappings=" + MAPPINGS_VERSION + "\n";
        for (String hash : hashes)
            out += hash + "\n";
        try {
            Files.write(md5info.toPath(), out.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void runSpecialSource(File mappingsFile, File inJar, File outJar) {
        for (Provider p : Remapper.providers) {
            boolean b = p.runSpecialSource(mappingsFile, inJar, outJar);
            if (b) return;
        }

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