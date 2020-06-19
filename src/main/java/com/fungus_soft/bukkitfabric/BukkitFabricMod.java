package com.fungus_soft.bukkitfabric;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.fungus_soft.bukkitfabric.command.VersionCommand;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class BukkitFabricMod implements ModInitializer {

    private Logger LOGGER = BukkitLogger.getLogger();

    @Override
    public void onInitialize() {
        LOGGER.info("Copyright \u00A9 2020 Fungus Software");

        boolean debug = FabricLoader.getInstance().isDevelopmentEnvironment();

        if (System.getProperty("IReallyKnowWhatIAmDoingISwear") == null && !debug) {
            int outdated = VersionCommand.check();
            if (outdated > 8) {
                try {
                    int time = outdated > 20 ? 40 : 20;
                    LOGGER.warning("*** Error, this build is outdated ***");
                    LOGGER.warning("*** Please download a new build from https://curseforge.com/minecraft/mc-mods/bukkit ***");
                    LOGGER.warning("*** Server will start in " + time + " seconds ***");
                    Thread.sleep(TimeUnit.SECONDS.toMillis(time));
                } catch (Exception e) {
                    LOGGER.warning(e.getMessage());
                }
            }
        }
    }

}