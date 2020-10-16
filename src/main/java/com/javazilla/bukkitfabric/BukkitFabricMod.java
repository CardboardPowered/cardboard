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
package com.javazilla.bukkitfabric;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.javazilla.bukkitfabric.impl.command.VersionCommand;
import com.javazilla.bukkitfabric.nms.MappingsReader;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class BukkitFabricMod implements ModInitializer {

    public static Logger LOGGER = BukkitLogger.getLogger();

    @Override
    public void onInitialize() {
        LOGGER.info("Bukkit for Fabric Mod");
        LOGGER.info("Copyright (C) 2020, Javazilla.com");

        boolean debug = FabricLoader.getInstance().isDevelopmentEnvironment();

        if (System.getProperty("SkipBukkitVersionCheck") == null && !debug) {
            int outdated = VersionCommand.check();
            if (outdated > 8) {
                try {
                    int time = outdated > 20 ? 40 : 10;
                    LOGGER.warning("*** Error, this build is outdated ***");
                    LOGGER.warning("*** Please download a new build from https://curseforge.com/minecraft/mc-mods/bukkit ***");
                    LOGGER.warning("*** Server will start in " + time + " seconds ***");
                    Thread.sleep(TimeUnit.SECONDS.toMillis(time));
                } catch (Exception e) {
                    LOGGER.warning(e.getMessage());
                }
            }
        }

        LOGGER.info("Loading spigot->intermediary mappings...");
        try {
            MappingsReader.main(null);
            LOGGER.info("Loaded spigot->intermediary mappings...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}