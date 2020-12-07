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
package com.javazilla.bukkitfabric.mixin;

import java.util.logging.LogRecord;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLogger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.BukkitLogger;

@Mixin(value = PluginLogger.class, remap = false)
public class MixinPluginLogger {

    @Shadow
    public String pluginName;

    public BukkitLogger BF_LOGGER;

    @Inject(at = @At("TAIL"), method = "<init>*")
    public void setBF(Plugin context, CallbackInfo ci) {
        this.BF_LOGGER = BukkitLogger.getLogger();
    }

    /**
     * @reason .
     * @author .
     */
    @Overwrite
    public void log(LogRecord logRecord) {
        logRecord.setMessage(pluginName + logRecord.getMessage());
        BF_LOGGER.log(logRecord);
    }

}