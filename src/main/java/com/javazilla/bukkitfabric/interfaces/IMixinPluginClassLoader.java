package com.javazilla.bukkitfabric.interfaces;

public interface IMixinPluginClassLoader {

    public Class<?> findClassBF(String name, boolean b) throws ClassNotFoundException;

}