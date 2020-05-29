package com.fungus_soft.bukkitfabric;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.fungus_soft.bukkitfabric.FakeLogger;
import com.fungus_soft.bukkitfabric.command.VersionCommand;

import net.fabricmc.api.ModInitializer;

public class BukkitFabricMod implements ModInitializer {

    private Logger LOGGER = FakeLogger.getLogger();

    @Override
    public void onInitialize() {
        LOGGER.info("Copyright \u00A9 2020 Fungus Software");

        if (System.getProperty("IReallyKnowWhatIAmDoingISwear") == null) {
            int update = VersionCommand.check();
            if (update > 8) {
                try {
                    LOGGER.warning("*** Error, this build is outdated ***");
                    LOGGER.warning("*** Please download a new build from https://curseforge.com/minecraft/mc-mods/bukkit ***");
                    LOGGER.warning("*** Server will start in 20 seconds ***");
                    Thread.sleep(TimeUnit.SECONDS.toMillis(20));
                } catch (Exception e) {
                    LOGGER.warning(e.getMessage());
                }
            }
        }
    }

}