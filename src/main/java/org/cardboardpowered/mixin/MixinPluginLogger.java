/**
 * CardboardPowered - Bukkit/Spigot for Fabric
 * Copyright (C) CardboardPowered.org and contributors
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either 
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.cardboardpowered.mixin;

import com.destroystokyo.paper.utils.PaperPluginLogger;
import com.javazilla.bukkitfabric.BukkitLogger;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.logging.LogRecord;
import java.util.logging.Logger;

@Mixin(value = PaperPluginLogger.class, remap = false)
public class MixinPluginLogger {

    @Overwrite
    @NotNull
    public static Logger getLogger(PluginDescriptionFile des) {
	    return BukkitLogger.getPluginLogger(des.getName());
    }

    public void log(LogRecord logRecord) {
        BukkitLogger.getLogger().log(logRecord);
    }

}
