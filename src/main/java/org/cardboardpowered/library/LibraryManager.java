package org.cardboardpowered.library;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Collection;
import javax.net.ssl.HttpsURLConnection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

/**
 * Simple library manager which downloads external dependencies.
 */
public final class LibraryManager {

    private final Logger logger = LogManager.getLogger("Cardboard");

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

    // URL to download Paper API
    private static final String PAPER_MAVEN = "https://repo.papermc.io/repository/maven-snapshots";

    // Apparently, repo.papermc.io is not stable.
    private static final String[] BACKUP = {
    		"https://web.archive.org/web/20250518123011/https://repo.papermc.io/repository/maven-snapshots/io/papermc/paper/paper-api/1.21.4-R0.1-SNAPSHOT/paper-api-1.21.4-R0.1-20250511.205801-225.jar"
    };

    /**
     * Creates the instance.
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
        if (!directory.isDirectory() && !directory.mkdirs()) {
            logger.error("Could not create libraries directory: " + directory);
        }

        // Load all libraries
        for (Library lib : libraries) {
        	String fn = lib.libraryKey.artifactId + "-" + lib.version + ".jar";
        	File f = new File(directory, fn);
        	if (f.isFile() && !(fn.contains("intermediary-adapter")) ) {
        		try {
        			KnotHelper.propose(f);
        		} catch (MalformedURLException e) {
        			e.printStackTrace();
        		}
        	} else {
        		download(lib);
        	}
        }

        KnotHelper.LOGGER.info("Loaded Adventure: " + KnotHelper.ver_adventure + " (" + KnotHelper.loaded_adventure + ")");
        KnotHelper.LOGGER.info("Loaded " + KnotHelper.loaded + " libraries.");
    }
    
    private String read_central_checksum(String repository, Library library) throws IOException {
    	
    	if (library.libraryKey.artifactId.contains("paper-api")) {
    		return "paper";
    	}

        URL url = null;

        try {
            url = new URL(repository + library.libraryKey.groupId.replace('.', '/') + '/' + library.libraryKey.artifactId + '/' + library.version
                    + '/' + library.libraryKey.artifactId + '-' + library.version + ".jar.sha1");
        } catch (MalformedURLException e) {
            try {
                // fallback
                url = new URL("https://web.archive.org/web/20250518125104/https://repo.papermc.io/repository/maven-snapshots/io/papermc/paper/paper-api/1.21.4-R0.1-SNAPSHOT/paper-api-1.21.4-R0.1-20250511.205801-225.jar.sha1");
            } catch (MalformedURLException e2) {
                logger.error("Failed to create URL for checksum: " + e2.getMessage());
                return null;
            }
        }
    	
        URLConnection urlc = url.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(urlc.getInputStream(), "UTF-8"));
        String inputLine;
        StringBuilder a = new StringBuilder();
        while ((inputLine = in.readLine()) != null)
        a.append(inputLine);
        in.close();

        return a.toString();
    }
    
    public void download(Library library) {
        String repository = library.repository;
        if (repository == null) repository = defaultRepository;
        if (!repository.endsWith("/")) repository += "/";

        String fileName = library.libraryKey.artifactId + '-' + library.version + ".jar";

        File file = new File(directory, fileName);
        if (!file.exists()) {
            int attempts = 0;
            while (attempts < maxDownloadAttempts) {
                attempts++;
                // download it
                logger.info("Downloading " + library.toString() + "...");
                try {
                    URL downloadUrl;
                    if (library.libraryKey.artifactId.contains("adapter")) {
                        downloadUrl = new URL("https://github.com/CardboardPowered/worldedit-adapters/releases/download/1.17.1/we-adapter-for-cardboard.jar");
                    } else if (null == library.libraryKey.spigotJarVersion) {
                        downloadUrl = new URL(repository + library.libraryKey.groupId.replace('.', '/') + '/' + library.libraryKey.artifactId + '/' + library.version
                                + '/' + library.libraryKey.artifactId + '-' + library.version + ".jar");
                    } else {
                    	
                    	String ver = library.version.split("-R0.1")[0];

                    	String paper_jar = PAPER_MAVEN + "/io/papermc/paper/paper-api/" + ver + "-R0.1-SNAPSHOT/paper-api-" + library.version + ".jar";

                    	downloadUrl = new URL(paper_jar);
                    	
                    	// downloadUrl = new URL(PAPER_URL);
                    }
                    HttpsURLConnection connection = (HttpsURLConnection) downloadUrl.openConnection();
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0 Chrome/90.0.4430.212");

                    try (
                    		ReadableByteChannel input = Channels.newChannel(connection.getInputStream());
                    		FileOutputStream output = new FileOutputStream(file)
                    	) {
                        output.getChannel().transferFrom(input, 0, Long.MAX_VALUE);
                        logger.info("Downloaded " + library.toString() + '.');
                    } catch (FileNotFoundException ex) {

                    	logger.info("Note: repo.papermc.io is misbehaving. Attempting backup URL..");

                    	
                    	for (String urll : BACKUP) {
	                    	downloadUrl = new URL(urll);
                            connection = (HttpsURLConnection) downloadUrl.openConnection();
	                    	try (
	                        		ReadableByteChannel input = Channels.newChannel(connection.getInputStream());
	                        		FileOutputStream output = new FileOutputStream(file)
	                        	) {
	                            output.getChannel().transferFrom(input, 0, Long.MAX_VALUE);
	                            logger.info("Downloaded " + library.toString() + '.');
	                        } catch (FileNotFoundException ex2) {
	                        	ex2.printStackTrace();
	                        }
                    	}
                    }

                    if (validateChecksum && library.checksumType != null && library.checksumValue != null && !checksum(file, library)) {
                        
                    	// logger.error("The checksum of '" + fileName + "' does not match. " + (attempts == maxDownloadAttempts ?
                        //        "Restart the server to attempt downloading it again." : "Attempting download again ("+ (attempts+1) +"/"+ maxDownloadAttempts +")"));
                        
                    	String rc = read_central_checksum(repository, library);
                    	System.out.println("RC:" + rc);
                    	
                    	if (rc.equals(library.checksumValue2)) {
	                        logger.info("The checksum of '" + fileName + "' (" + rc + ") has matched.");
                    	} else {
                        
	                        logger.error("The checksum of '" + fileName + "' does not match. Found: " + library.checksumValue2 + ", Need: " + library.checksumValue);
	                        
	                        file.delete();
	                        
	                        if (attempts == maxDownloadAttempts) return;
	                        continue;
                    	}
                    }
                    // everything's fine
                    break;
                } catch (IOException e) {
                    logger.warn( "Failed to download: " + library.toString(), e);
                    file.delete();
                    if (attempts == maxDownloadAttempts) {
                        logger.warn("Restart the server to attempt downloading '" + fileName + "' again.");
                        return;
                    }
                    logger.warn("Attempting download of '" + fileName + "' again (" + (attempts + 1) + "/" + maxDownloadAttempts + ")");
                }
            }
        } else if (validateChecksum && library.checksumType != null && library.checksumValue != null && !checksum(file, library)) {
            // The file is already downloaded, but validate the checksum as a warning only
            logger.warn("The checksum for the library '" + fileName + "' does not match. Remove the library and restart the server to download it again.");
        }

        // Add to KnotClassLoader
        try {
            if (!library.libraryKey.artifactId.contains("adapter")) {
                KnotHelper.propose(file);
            }
        } catch (Exception e) {
            logger.warn( "Failed to add to classpath: " + library.toString(), e);
        }
    }

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
            library.checksumValue2 = digest;
            
            if (checksum.equalsIgnoreCase("giveme")) {
            	logger.info("Hash for " + file.getName() + " = " + digest);
            }
            
        } catch (IOException ex) {
            logger.error("Failed to compute digest for '" + file.getName() + "'", ex);
            return false;
        }
        // System.out.println(file.getName() + ": " + digest);
        return digest.equals(checksum);
    }

    /**
     * An enum containing the supported hash algorithms.
     */
    public enum HashAlgorithm {
        SHA1(Hashing.sha1()), // The SHA-1 hash algorithm.
        MD5(Hashing.md5()); // The MD5 hash algorithm.

        public final HashFunction function;

        private HashAlgorithm(HashFunction function) {
            this.function = function;
        }
    }

}