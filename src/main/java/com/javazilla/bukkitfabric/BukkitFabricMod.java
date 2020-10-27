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
import java.util.logging.Logger;

import com.javazilla.bukkitfabric.nms.MappingsReader;

import net.fabricmc.api.ModInitializer;

public class BukkitFabricMod implements ModInitializer {

    public static Logger LOGGER = BukkitLogger.getLogger();

    @Override
    public void onInitialize() {
        LOGGER.info("Bukkit for Fabric Mod - Javazilla.com");

        try {
            MappingsReader.main(null);
            LOGGER.info("Loaded spigot->intermediary mappings.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        LOGGER.info("Bukkt4Fabric Loaded.");
    }

}