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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.cardboardpowered.library.Library;
import org.cardboardpowered.library.LibraryKey;
import org.cardboardpowered.library.LibraryManager;

import com.google.common.collect.ImmutableMap;
import com.javazilla.bukkitfabric.nms.MappingsReader;

import net.fabricmc.api.ModInitializer;
import net.minecraft.server.network.ServerLoginNetworkHandler;

@SuppressWarnings("deprecation")
public class BukkitFabricMod implements ModInitializer {

    public static Logger LOGGER = BukkitLogger.getLogger(); 
    public static boolean isAfterWorldLoad = false;
    public static final Random random = new Random();

    public static List<ServerLoginNetworkHandler> NETWORK_CASHE = new ArrayList<>();

    @Override
    public void onInitialize() {
        LOGGER.info("Bukkit for Fabric Mod - Javazilla.com");

        loadLibs();

        try {
            MappingsReader.main(null);
            //if (FabricLoader.getInstance().isDevelopmentEnvironment())
            //    IngotReader.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        LOGGER.info("Bukkt4Fabric Loaded.");
    }

    public static void loadLibs() {
        String repository = "https://repo.glowstone.net/repository/maven-public/";

        Map<LibraryKey, Library> libraries = Stream.of(
                new Library("org.xerial", "sqlite-jdbc", "3.21.0.1", LibraryManager.HashAlgorithm.SHA1, "81a0bcda2f100dc91dc402554f60ed2f696cded5"),
                new Library("mysql", "mysql-connector-java", "5.1.46", LibraryManager.HashAlgorithm.SHA1, "9a3e63b387e376364211e96827bc27db8d7a92e9")
            ).collect(ImmutableMap.toImmutableMap(Library::getLibraryKey, Function.identity()));
        new LibraryManager(repository, "lib", true, 2, libraries.values()).run();
    }

}