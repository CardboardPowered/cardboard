package com.javazilla.bukkitfabric;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.plugin.Plugin;

import com.destroystokyo.paper.Metrics;

public class PaperMetrics {
    public static void startMetrics() {
        // Get the config file
        File configFile = new File(new File(new File("plugins"), "bStats"), "config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        // Check if the config file exists
        if (!config.isSet("serverUuid")) {

            // Add default values
            config.addDefault("enabled", true);
            // Every server gets it's unique random id.
            config.addDefault("serverUuid", UUID.randomUUID().toString());
            // Should failed request be logged?
            config.addDefault("logFailedRequests", false);

            // Inform the server owners about bStats
            config.options().header(
                    "bStats collects some data for plugin authors like how many servers are using their plugins.\n" 
                            + "To honor their work, you should not disable it.\n" 
                            + "This has nearly no effect on the server performance!\n" 
                            + "Check out https://bStats.org/ to learn more :)"
            ).copyDefaults(true);
            try {
                config.save(configFile);
            } catch (IOException ignored) {
            }
        }
        // Load the data
        String serverUUID = config.getString("serverUuid");
        boolean logFailedRequests = config.getBoolean("logFailedRequests", false);
        // Only start Metrics, if it's enabled in the config
        if (config.getBoolean("enabled", true)) {
            Metrics metrics = new Metrics("Paper", serverUUID, logFailedRequests, Bukkit.getLogger());

            metrics.addCustomChart(new Metrics.SimplePie("minecraft_version", () -> {
                String minecraftVersion = Bukkit.getVersion();
                minecraftVersion = minecraftVersion.substring(minecraftVersion.indexOf("MC: ") + 4, minecraftVersion.length() - 1);
                return minecraftVersion;
            }));

            metrics.addCustomChart(new Metrics.SingleLineChart("players", () -> Bukkit.getOnlinePlayers().size()));
            metrics.addCustomChart(new Metrics.SimplePie("online_mode", () -> Bukkit.getOnlineMode() ? "online" : "offline"));
            metrics.addCustomChart(new Metrics.SimplePie("paper_version", () -> "git-Cardboard-Fabric"));

            
            metrics.addCustomChart(new Metrics.DrilldownPie("java_version", () -> {
                Map<String, Map<String, Integer>> map = new HashMap<>();
                String javaVersion = System.getProperty("java.version");
                Map<String, Integer> entry = new HashMap<>();
                entry.put(javaVersion, 1);

                // http://openjdk.java.net/jeps/223
                // Java decided to change their versioning scheme and in doing so modified the java.version system
                // property to return $major[.$minor][.$secuity][-ea], as opposed to 1.$major.0_$identifier
                // we can handle pre-9 by checking if the "major" is equal to "1", otherwise, 9+
                String majorVersion = javaVersion.split("\\.")[0];
                String release;

                int indexOf = javaVersion.lastIndexOf('.');

                if (majorVersion.equals("1")) {
                    release = "Java " + javaVersion.substring(0, indexOf);
                } else {
                    // of course, it really wouldn't be all that simple if they didn't add a quirk, now would it
                    // valid strings for the major may potentially include values such as -ea to deannotate a pre release
                    Matcher versionMatcher = Pattern.compile("\\d+").matcher(majorVersion);
                    if (versionMatcher.find()) {
                        majorVersion = versionMatcher.group(0);
                    }
                    release = "Java " + majorVersion;
                }
                map.put(release, entry);

                return map;
            }));

            metrics.addCustomChart(new Metrics.DrilldownPie("legacy_plugins", () -> {
                Map<String, Map<String, Integer>> map = new HashMap<>();

                // count legacy plugins
                int legacy = 0;
                for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                    if (CraftMagicNumbers.isLegacy(plugin.getDescription())) {
                        legacy++;
                    }
                }

                // insert real value as lower dimension
                Map<String, Integer> entry = new HashMap<>();
                entry.put(String.valueOf(legacy), 1);

                // create buckets as higher dimension
                if (legacy == 0) {
                    map.put("0 \uD83D\uDE0E", entry); // :sunglasses:
                } else if (legacy <= 5) {
                    map.put("1-5", entry);
                } else if (legacy <= 10) {
                    map.put("6-10", entry);
                } else if (legacy <= 25) {
                    map.put("11-25", entry);
                } else if (legacy <= 50) {
                    map.put("26-50", entry);
                } else {
                    map.put("50 \uD83D\uDE2D", entry); // :cry:
                }

                return map;
            }));
        }

    }
}