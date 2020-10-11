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

    @Overwrite
    public void log(LogRecord logRecord) {
        logRecord.setMessage(pluginName + logRecord.getMessage());
        BF_LOGGER.log(logRecord);
    }

}