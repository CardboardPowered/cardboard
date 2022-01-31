package org.cardboardpowered.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JarReader {

    public static List<String> found = new ArrayList<>();
    private static final Logger logger = LogManager.getLogger("Cardboard");

    public static void read_plugins(File folder) throws Exception {
        logger.info("Please wait, Scanning plugins for events...");
        long start = System.currentTimeMillis();
        for (File f : folder.listFiles()) {
            if (f.getName().endsWith(".jar")) {
                read_jar(f.getAbsolutePath());
            }
        }

        // for (String s : found) System.out.println(s);
        logger.info("Found: " + found.size() + " (Took: " + (System.currentTimeMillis() - start) + "ms)");
    }

    public static void read_jar(String path) throws Exception {
        ZipFile zipFile = new ZipFile(path);

        Enumeration<? extends ZipEntry> entries = zipFile.entries();

        while(entries.hasMoreElements()){
            ZipEntry entry = entries.nextElement();
            InputStream stream = zipFile.getInputStream(entry);
            if (!entry.getName().endsWith(".class")) continue;

            // convert stream into a reader
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String contents = reader.lines().collect(Collectors.joining("\n"));

            for (String event : KnownEvents.EVENTS) {
                if (contents.contains(event) && !found.contains(event) ) {
                    found.add(event);
                }
            }
            
            
            stream.close();
        }
        zipFile.close();
    }

}
