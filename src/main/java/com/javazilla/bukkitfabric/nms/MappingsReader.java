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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.techcable.srglib.FieldData;
import net.techcable.srglib.JavaType;
import net.techcable.srglib.format.MappingsFormat;
import net.techcable.srglib.mappings.Mappings;

public class MappingsReader {

    public static Mappings MAPPINGS;
    public static HashMap<String, String> METHODS;
    public static HashMap<String, String> METHODS2;

    public static Logger LOGGER = LogManager.getLogger("BukkitNmsRemapper");

    public static void main(String[] args) throws IOException {
        File dir = new File("mappings");
        dir.mkdirs();
        File f = exportResource("spigot2intermediary.csrg", dir);
        MAPPINGS = MappingsFormat.COMPACT_SEARGE_FORMAT.parseFile(f);
        METHODS = new HashMap<>();
        METHODS2 = new HashMap<>();
        LOGGER.info("Reflection working: " + MAPPINGS.getNewClass("net.minecraft.server.MinecraftKey").getName().equalsIgnoreCase("net.minecraft.class_2960"));

        MAPPINGS.forEachMethod((spigot, intermed) -> {
            String sN = spigot.getName();
            String iN = intermed.getName();
            String clazz = intermed.getDeclaringType().getName();

            METHODS.put(clazz + "=" + sN, iN);
            boolean put = true;
            if (METHODS2.containsKey(sN + intermed.getSignature().getDescriptor())) {
                METHODS2.remove(sN + intermed.getSignature().getDescriptor());
                put = false;
            }
            if (sN.length() > 2 && iN.length() > 2 && put) METHODS2.put(sN + intermed.getSignature().getDescriptor(), iN);
        });
        System.out.println(METHODS2.get("setInvisible(Z)V"));
    }

    public static String getIntermedClass(String spigot) {
        return MAPPINGS.getNewClass(spigot).getName();
    }

    public static String getIntermedField(String c, String spigot) throws NoSuchFieldException, SecurityException, ClassNotFoundException {
        JavaType type = JavaType.fromName(getIntermedClass(c));
        if (c.contains("class_"))
            type = MAPPINGS.inverted().getNewClass(c);
        return MAPPINGS.getNewField(FieldData.create(type, spigot)).getName();
    }

    public static File exportResource(String res, File folder) {
        try (InputStream stream = MappingsReader.class.getClassLoader().getResourceAsStream("mappings/" + res)) {
            if (stream == null) throw new IOException("Null " + res);

            Path p = Paths.get(folder.getAbsolutePath() + File.separator + res);
            Files.copy(stream, p, StandardCopyOption.REPLACE_EXISTING);
            return p.toFile();
        } catch (IOException e) { e.printStackTrace(); return null;}
    }

    public static String getIntermedMethod(String name, String spigot) {
        // TODO This very bad. It doesn't use the method descriptor.
        // TODO There are 44 spigot-named methods that will have duplicates.
        return METHODS.getOrDefault(name + "=" + spigot, spigot);
    }

}