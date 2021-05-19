package org.cardboardpowered;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;

import com.javazilla.bukkitfabric.nms.ReflectionMethodVisitor;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Manual parser for YAML, as I may move BukkitAPI to the library
 * manager in the future which would get rid of the YAMLConfig API.
 * Also as this will mainly be used with just boolean/String it should
 * be fine to manually parse.
 */
public class CardboardConfig {

    private static String DEFAULT = 
             "# This is the configuration file for Cardboard.\n\n" +
             "# Invoke ChatEvent from PlayerManager instead of NetworkHandler. This may solve conflicts with\n" +
             "# mods that inject into the chat method, but will not invoke the deprecated sync chat event\n" +
             "use_alternative_chat_mixin=false\n" +
             "\n"+
             "# Reflection Remapper Skip\n" +
             "# Our current Reflection remapper might cause issues with some plugins\n" +
             "# You can add plugin names here (that dont use Reflection) to our SKIP array\n" +
             "skip_reflection_for_plugin=vault\n" +
             "skip_reflection_for_plugin=worldguard\n"
             ;

    public static ArrayList<String> disabledMixins = new ArrayList<>();
    public static boolean ALT_CHAT = false;

    public static void setup() throws Exception {
        File f = new File(FabricLoader.getInstance().getConfigDir().toFile(), "cardboard.yml");
        if (!f.exists()) {
            f.createNewFile();
            Files.write(f.toPath(), DEFAULT.getBytes());
        }

        for (String line : Files.readAllLines(f.toPath())) {
            if (line.startsWith("#")) continue;
            if (line.indexOf('=') != -1) {
                line = line.trim();
                String val = line.split("=")[1];
                if (line.startsWith("use_alternative_chat_mixin")) ALT_CHAT = Boolean.valueOf(val);
                if (line.startsWith("mixin_force_disable")) {
                    if (val.startsWith("org.cardboardpowered.mixin."))
                        disabledMixins.add(val);
                    else disabledMixins.add("org.cardboardpowered.mixin." + val);
                }
                if (line.startsWith("skip_reflection_for_plugin")) ReflectionMethodVisitor.SKIP.add(val);
            }
        }
    }

}