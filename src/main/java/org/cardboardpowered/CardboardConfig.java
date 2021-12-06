package org.cardboardpowered;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

import net.fabricmc.loader.api.FabricLoader;

import me.isaiah.config.FileConfiguration;

public class CardboardConfig {
    
    private static String DEFAULT = 
            "# This is the configuration file for Cardboard\n\n" +
            "# Invoke ChatEvent from PlayerManager instead of NetworkHandler\n" +
            "# This can solve issues with other mods that overwrite the chat method,\n" +
            "use_alternative_chat_mixin: false\n" +
            "\n"+
            "# Reflection Remapper Skip\n" +
            "# Our current Reflection remapper might cause issues with some plugins\n" +
            "# You can add plugin names here (that dont use Reflection) to our SKIP array\n" +
            "skip_reflection_for_plugin:\n\t- vault\n\t- worldguard\n\n" +
            "# Forcefully Disable Mixins - If a mixin is causing an issue you can disable it here\n" +
            "mixin-force-disable:\n\t- None"
            ;

    public static ArrayList<String> disabledMixins = new ArrayList<>();
    public static boolean ALT_CHAT = false;

    @SuppressWarnings("unchecked")
    public static void setup() throws Exception {
        File fabDir = FabricLoader.getInstance().getConfigDir().toFile();
        File oldDir = new File(fabDir, "bukkit4fabric");
        File dir = new File(fabDir, "cardboard");
        if (oldDir.exists()) {
            for (File fi : oldDir.listFiles()) fi.renameTo(new File(dir, fi.getName()));
            oldDir.delete();
        }

        dir.mkdirs();
        File f = new File(dir, "cardboard-config.yml");
        save_default(f);

        File oldConfig = new File(fabDir, "cardboard.yml");
        if (oldConfig.exists()) {
            migrate_config(oldConfig, f);
        }

        FileConfiguration config = new FileConfiguration(f);
        ALT_CHAT = config.getBoolean("use_alternative_chat_mixin");

        ArrayList<String> disables = (ArrayList<String>)config.getObject("mixin-force-disable");
        disabledMixins.addAll(disables);
    }

    private static void save_default(File file) throws IOException {
        if (!file.exists()) {
            file.createNewFile();
            Files.write(file.toPath(), DEFAULT.getBytes());
        }
    }

    private static void migrate_config(File oldConfig, File newConfig) throws IOException {
        System.out.println("Migrating old configuration...");
        for (String line : Files.readAllLines(oldConfig.toPath())) {
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
            }
        }

        String con = "";
        for (String line : Files.readAllLines(newConfig.toPath())){con += line + "\n";}
        con = con.replace("use_alternative_chat_mixin: false", "use_alternative_chat_mixin: " + ALT_CHAT);
        Files.write(newConfig.toPath(), con.getBytes());
        oldConfig.delete();
    }

}
