package com.javazilla.bukkitfabric.interfaces;

public interface IMixinPluginClassLoader {

    Class<?> findClassBF(String name, boolean b) throws ClassNotFoundException;

}