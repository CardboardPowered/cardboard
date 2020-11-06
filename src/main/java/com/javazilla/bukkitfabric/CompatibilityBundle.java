package com.javazilla.bukkitfabric;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableMap;

import net.glowstone.util.library.Library;
import net.glowstone.util.library.LibraryKey;
import net.glowstone.util.library.LibraryManager;

/**
 * Compatibility bundles are bundles of libraries that Spigot includes but Fabric does not.
 */
public enum CompatibilityBundle {

    CRAFTBUKKIT(
        Stream.of(
            new Library("org.xerial", "sqlite-jdbc", "3.21.0.1", LibraryManager.HashAlgorithm.SHA1, "81a0bcda2f100dc91dc402554f60ed2f696cded5"),
            new Library("mysql", "mysql-connector-java", "5.1.46", LibraryManager.HashAlgorithm.SHA1, "9a3e63b387e376364211e96827bc27db8d7a92e9")
        ).collect(ImmutableMap.toImmutableMap(Library::getLibraryKey, Function.identity()))
    ),
    NONE(ImmutableMap.of());

    public final Map<LibraryKey, Library> libraries;

    CompatibilityBundle(Map<LibraryKey, Library> libraries) {
        this.libraries = libraries;
    }

}