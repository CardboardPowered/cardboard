package com.javazilla.bukkitfabric.mixin;

import org.bukkit.plugin.java.PluginClassLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.javazilla.bukkitfabric.interfaces.IMixinPluginClassLoader;

@Mixin(value = PluginClassLoader.class, remap = false)
public class MixinPluginClassLoader implements IMixinPluginClassLoader {

    @Override
    public Class<?> findClassBF(String name, boolean b) throws ClassNotFoundException {
        return findClass(name, b);
    }

    @Shadow
    Class<?> findClass(String name, boolean checkGlobal) throws ClassNotFoundException {
        return null;
    }

}