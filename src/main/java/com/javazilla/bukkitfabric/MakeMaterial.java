package com.javazilla.bukkitfabric;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;

import org.bukkit.NamespacedKey;
import com.google.common.base.Preconditions;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.launch.knot.Knot;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import static com.javazilla.bukkitfabric.BukkitFabricMod.LOGGER;

public class MakeMaterial {

    // TODO: This needs to be kept updated when Spigot updates
    // It is the value of Material.values().length
    public static int MATERIAL_LENGTH = 1540;

    public static void make() throws IOException {
        Path p = FabricLoader.getInstance().getModContainer("bukkitfabric").get().getPath("org/bukkit/Material.java1");
        File f = new File("lib", "Material.java");
        Files.copy(p, f.toPath(), StandardCopyOption.REPLACE_EXISTING);

        List<String> lines = Files.readAllLines(f.toPath());
        List<String> newlines = new ArrayList<>();
        for (String str : lines) {
            newlines.add(str);
            if (str.trim().startsWith("// CARDBOARD MATERIAL START PLACE")) {
                List<String> toAdd = setupUnknownModdedMaterials();
                for (String add : toAdd)
                    newlines.add("    " + add);
            }
        }
        Files.write(f.toPath(), newlines, StandardCharsets.UTF_8);

        compile(f, new Diagnostics(), "-proc:none", "-cp", getCP());

        File mc1 = new File("lib", "Material.class");
        File mc2 = new File("lib", "Material$1.class");

        File f2 = new File("lib", "Cardboard-Generated-Material-Class.jar");
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(f2));
        ZipEntry e = new ZipEntry("org/bukkit/Material.class");
        out.putNextEntry(e);

        byte[] data = Files.readAllBytes(mc1.toPath());
        out.write(data, 0, data.length);
        out.closeEntry();

        ZipEntry e2 = new ZipEntry("org/bukkit/Material$1.class");
        out.putNextEntry(e2);
        byte[] data2 = Files.readAllBytes(mc2.toPath());
        out.write(data2, 0, data2.length);
        out.closeEntry();
        out.close();
        mc1.delete();
        mc2.delete();
        //f.delete();
        LOGGER.info("Adding \"" + f2.getName() + "\" to Knot.");
        Knot.getLauncher().propose(f2.toURI().toURL());
    }

    public static String getCP() {
        String cp = "";
        File mods = new File("mods");
        for (File f : mods.listFiles())
            cp += f.getAbsolutePath() + File.pathSeparator;
        File r = new File("lib");
        for (File f : r.listFiles())
            if (!f.getName().contains("Cardboard-Generated"))
                cp += f.getAbsolutePath() + File.pathSeparator;
        File rj = new File(new File(".fabric"), "remappedJars");
        if (rj.listFiles() != null)
            for (File f : rj.listFiles())
                cp += f.getAbsolutePath() + File.separator + "intermediary-server.jar" + File.pathSeparator;
        cp += ".";
        return cp;
    }

    public static String standardize(Identifier location) {
        Preconditions.checkNotNull(location, "location");
        return (location.getNamespace().equals(NamespacedKey.MINECRAFT) ? location.getPath() : location.toString())
            .replace(':', '_')
            .replaceAll("\\s+", "_")
            .replaceAll("\\W", "")
            .toUpperCase(Locale.ENGLISH);
    }

    public static List<String> setupUnknownModdedMaterials() {
        int i = MATERIAL_LENGTH; 

        List<String> names = new ArrayList<>();
        List<String> list = new ArrayList<>();
        for (Block block : Registry.BLOCK) {
            Identifier id = Registry.BLOCK.getId(block);
            String name = standardize(id);
            if (id.getNamespace().startsWith("minecraft")) continue;

            list.add(name + "(" + i + "," + " new org.cardboardpowered.impl.CardboardModdedBlock(\"" + id.toString() + "\") ),");
            names.add(name);

            i++;
            BukkitFabricMod.LOGGER.info("Registered modded '" + id + "' as Material '" + name + "'");
        }

        for (Item item : Registry.ITEM) {
            Identifier id = Registry.ITEM.getId(item);
            String name = standardize(id);
            if (id.getNamespace().startsWith("minecraft") || names.contains(name)) continue;

            list.add(name + "(" + i + "," + " new org.cardboardpowered.impl.CardboardModdedItem(\"" + id.toString() + "\") ),");
            names.add(name);

            i++;
            BukkitFabricMod.LOGGER.info("Registered modded '" + id + "' as Material '" + name + "'");
        }
        return list;
    }

    private static class Diagnostics implements DiagnosticListener<JavaFileObject> {
        @Override
        public void report(Diagnostic<? extends JavaFileObject> event) {
            String msg = event.getMessage(null);
            if (!msg.startsWith("Recompile with -X")) LOGGER.info("JavaC Debug: " + msg);
        }
    }

    public static boolean compile(File source, DiagnosticListener<JavaFileObject> diagnostics, String...list) {
        JavaCompiler javac = null;
        try {
            javac = (JavaCompiler) Class.forName("com.sun.tools.javac.api.JavacTool").newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        ArrayList<String> arguments = new ArrayList<String>();
        for (String s : list) arguments.add(s);
        StandardJavaFileManager fileManager = javac.getStandardFileManager(diagnostics, null, Charset.forName("UTF-8"));
        CompilationTask task = javac.getTask(null, fileManager, diagnostics, arguments, null, fileManager.getJavaFileObjectsFromFiles(Arrays.asList(source)));

        return task.call();
    }

}