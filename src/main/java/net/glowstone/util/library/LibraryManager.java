package net.glowstone.util.library;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.net.ssl.HttpsURLConnection;

import com.google.common.collect.Maps;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.javazilla.bukkitfabric.BukkitFabricMod;
import com.javazilla.bukkitfabric.LibraryClassLoader;

/**
 * Simple library manager which downloads external dependencies.
 */
public final class LibraryManager {

    /**
     * The Maven repository to download from.
     */
    private final String defaultRepository;

    /**
     * The directory to store downloads in.
     */
    private final File directory;

    /**
     * Whether the checksum of each library should be verified after being downloaded.
     */
    private final boolean validateChecksum;

    /**
     * The maximum amount of attempts to download each library.
     */
    private final int maxDownloadAttempts;

    private final Collection<Library> libraries;

    private final ExecutorService downloaderService = Executors.newCachedThreadPool();

    /**
     * Creates the instance.
     *
     * @param defaultRepository the repository to download the libraries from
     * @param directoryName the name of the directory to download the libraries to
     * @param validateChecksum whether or not checksum validation is enabled
     * @param maxDownloadAttempts the maximum number of attempts to download a library
     * @param libraries the libraries to download
     */
    public LibraryManager(String defaultRepository, String directoryName, boolean validateChecksum, int maxDownloadAttempts, Collection<Library> libraries) {
        checkNotNull(defaultRepository);
        checkNotNull(directoryName);
        this.defaultRepository = defaultRepository;
        this.directory = new File(directoryName);
        this.validateChecksum = validateChecksum;
        this.maxDownloadAttempts = maxDownloadAttempts;
        this.libraries = libraries;
    }

    /**
     * Downloads the libraries.
     */
    public void run() {
        if (!directory.isDirectory() && !directory.mkdirs())
            BukkitFabricMod.LOGGER.log(Level.SEVERE, "Could not create libraries directory: " + directory);

        for (Library library : libraries) downloaderService.execute(new LibraryDownloader(library));

        downloaderService.shutdown();
        try {
            if (!downloaderService.awaitTermination(1, TimeUnit.MINUTES)) downloaderService.shutdownNow();
        } catch (InterruptedException e) {
            BukkitFabricMod.LOGGER.log(Level.SEVERE, "Library Manager thread interrupted: ", e);
        }
    }

    private class LibraryDownloader implements Runnable {
        private final Library library;
        private final String repository;

        /**
         * Creates an instance of the downloader for a library.
         * @param library a {@link Library} instance representing a library
         */
        LibraryDownloader(Library library) {
            this.library = library;

            String repository = library.repository;
            if (repository == null) repository = defaultRepository;
            if (!repository.endsWith("/")) repository += "/";

            this.repository = repository;
        }

        @SuppressWarnings("resource")
        @Override
        public void run() {
            // check if we already have it
            File file = new File(directory, getLibrary());
            if (!file.exists()) {
                int attempts = 0;
                while (attempts < maxDownloadAttempts) {
                    attempts++;
                    // download it
                    BukkitFabricMod.LOGGER.info("Downloading " + library.toString() + "...");
                    try {
                        URL downloadUrl = new URL(repository + library.libraryKey.groupId.replace('.', '/') + '/' + library.libraryKey.artifactId + '/' + library.version
                                        + '/' + library.libraryKey.artifactId + '-' + library.version + ".jar");
                        HttpsURLConnection connection = (HttpsURLConnection) downloadUrl.openConnection();
                        connection.setRequestProperty("User-Agent", "Mozilla/5.0 Chrome/80");

                        try (ReadableByteChannel input = Channels.newChannel(connection.getInputStream()); FileOutputStream output = new FileOutputStream(file)) {
                            output.getChannel().transferFrom(input, 0, Long.MAX_VALUE);
                            BukkitFabricMod.LOGGER.info("Downloaded " + library.toString() + '.');
                        }

                        if (validateChecksum && library.checksumType != null && library.checksumValue != null && !checksum(file, library)) {
                            BukkitFabricMod.LOGGER.severe("The checksum for the library '" + getLibrary() + "' does not match. " + (attempts == maxDownloadAttempts ?
                                    "Restart the server to attempt downloading it again." : "Attempting download again ("+ (attempts+1) +"/"+ maxDownloadAttempts +")"));
                            file.delete();
                            if (attempts == maxDownloadAttempts) return;
                            continue;
                        }
                        // everything's fine
                        break;
                    } catch (IOException e) {
                        BukkitFabricMod.LOGGER.log(Level.WARNING, "Failed to download: " + library.toString(), e);
                        file.delete();
                        if (attempts == maxDownloadAttempts) {
                            BukkitFabricMod.LOGGER.warning("Restart the server to attempt downloading '" + getLibrary() + "' again.");
                            return;
                        }
                        BukkitFabricMod.LOGGER.warning("Attempting download of '" + getLibrary() + "' again (" + (attempts + 1) + "/" + maxDownloadAttempts + ")");
                    }
                }
            } else if (validateChecksum && library.checksumType != null && library.checksumValue != null && !checksum(file, library)) {
                // The file is already downloaded, but validate the checksum as a warning only
                BukkitFabricMod.LOGGER.warning("The checksum for the library '" + getLibrary() + "' does not match. Remove the library and restart the server to download it again.");
            }

            // hack it onto the PluginClassLoader
            try {
                new LibraryClassLoader(new URL[] {file.toURI().toURL()}, file);
            } catch (Exception e) {
                BukkitFabricMod.LOGGER.log(Level.WARNING, "Failed to add to classpath: " + library.toString(), e);
            }
        }

        /**
         * Gets the name of the file the library will be saved to.
         *
         * @return the name of the file the library will be saved to
         */
        String getLibrary() {
            return library.libraryKey.artifactId + '-' + library.version + ".jar";
        }

        /**
         * Computes and validates the checksum of a file.
         *
         * <p>If the file does not exist, the checksum will be automatically invalidated.
         * <p>If the reference checksum or the algorithm are empty or null, the checksum will be automatically validated.
         *
         * @param file the file.
         * @param library the {@link Library} instance containing the algorithm and the checksum.
         * @return true if the checksum was validated, false otherwise.
         */
        boolean checksum(File file, Library library) {
            checkNotNull(file);
            if (!file.exists()) return false;

            HashAlgorithm algorithm = library.checksumType;
            String checksum = library.checksumValue;
            if (algorithm == null || checksum == null || checksum.isEmpty()) return true; // assume everything is OK if no reference checksum is provided

            // get the file digest
            String digest;
            try {
                digest = Files.hash(file, algorithm.function).toString();
            } catch (IOException ex) {
                BukkitFabricMod.LOGGER.log(Level.SEVERE, "Failed to compute digest for '" + file.getName() + "'", ex);
                return false;
            }
            return digest.equals(checksum);
        }
    }

    /**
     * An enum containing the supported hash algorithms.
     */
    public enum HashAlgorithm {
        SHA1(Hashing.sha1(), "sha1"), // The SHA-1 hash algorithm.
        MD5(Hashing.md5(), "md5"); // The MD5 hash algorithm.

        public final HashFunction function;
        public final String name;

        private static final Map<String, HashAlgorithm> BY_NAME = Maps.newHashMap();

        /**
         * Represents a hash algorithm.
         *
         * @param function the {@link HashFunction} used to calculate the hash
         * @param name the name of the algorithm
         */
        HashAlgorithm(HashFunction function, String name) {
            checkNotNull(function);
            checkNotNull(name);

            this.function = function;
            this.name = name;
        }

        /**
         * Gets the hash algorithm corresponding to the given name.
         *
         * @param name the name of the algorithm
         * @return the corresponding algorithm, or null if none exists
         */
        public static HashAlgorithm getAlgorithm(String name) {
            checkNotNull(name);
            return BY_NAME.get(name.toLowerCase());
        }

        static {
            for (HashAlgorithm algorithm : values())  BY_NAME.put(algorithm.name, algorithm); // add the algorithms to the map
        }
    }

}