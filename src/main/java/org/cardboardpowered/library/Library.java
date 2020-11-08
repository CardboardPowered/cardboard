package org.cardboardpowered.library;

import com.google.common.collect.ComparisonChain;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.cardboardpowered.library.LibraryManager.HashAlgorithm;
import org.jetbrains.annotations.NonNls;

/**
 * Represents a library that will be injected into the PluginClassLoader at runtime.
 */
public class Library implements Comparable<Library> {

    // The group ID of the library in a maven-style repo. Parts of the group ID must be separated by periods.
    public final LibraryKey libraryKey;
    public LibraryKey getLibraryKey() {return libraryKey;}

    //The version number of the library in a maven-style repo.
    public final String version;

    // The optional URL for this library, for use in cases where the library is not part of the default Maven repo.
    public final String repository;

    // The algorithm used to generate the checksum for this library, if one was specified.
    public final HashAlgorithm checksumType;

    // The checksum itself, validated against the library to make sure the library is intact.
    public final String checksumValue;

    // Excludes the dependency from any dependency checks. Use this if the library is locally hosted.
    public final boolean excludeDependencies;

    /**
     * Creates a {@link Library} instance with the specified group ID, artifact ID, version, and checksum.
     */
    public Library(@NonNls String groupId, @NonNls String artifactId, @NonNls String version, HashAlgorithm checksumType, @NonNls String checksumValue) {
        this(groupId, artifactId, version, null, checksumType, checksumValue, false);
    }

    /**
     * Creates a {@link Library} instance with the specified group ID, artifact ID, version, repository, and checksum.
     * @param groupId The group ID of the library, separated by periods.
     * @param artifactId The artifact ID of the library.
     * @param version The version of the library.
     * @param repository The URL of the library's repository.
     * @param checksumType The type of hash the checksum is using.
     * @param checksumValue The checksum to validate the downloaded library against.
     * @param excludeDependencies Specifies that dependencies may be excluded.
     */
    public Library(String groupId, String artifactId, String version, String repository, HashAlgorithm checksumType, String checksumValue, boolean excludeDependencies) {
        this.libraryKey = new LibraryKey(groupId, artifactId);
        this.version = version;
        this.repository = StringUtils.isBlank(repository) ? null : repository;
        this.checksumType = checksumType;
        this.checksumValue = checksumValue;
        this.excludeDependencies = excludeDependencies;
    }

    @Override
    public String toString() {
        return libraryKey.toString() + ":" + version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Library library = (Library) o;
        return Objects.equals(libraryKey, library.libraryKey) && Objects.equals(version, library.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(libraryKey, version);
    }

    @Override
    public int compareTo(Library o) {
        return ComparisonChain.start().compare(libraryKey, o.libraryKey).result();
    }

}