package org.cardboardpowered.library;

import java.io.File;
import java.util.List;
import org.cardboardpowered.CardboardConfig;

import net.fabricmc.loader.api.FabricLoader;

public class Libraries {

	/**
	 * List of our/Paper Libraries to download/load.
	 * Including: Paper-API, Adventure, Bungee-api, etc.
	 * 
	 * @implNote "paper-api" version number != Paper server version number.
	 * @see https://artifactory.papermc.io/ui/native/universe/io/papermc/paper/paper-api/
	 * @see https://github.com/PaperMC/Paper/blob/main/paper-api/build.gradle.kts
	 */
	public static List<Library> getLibraries() {
        // TODO: Keep Adventure version in check
        String adventureVersion = "5.2.0"; // 26.2: Paper 26.2 ships Adventure 5

        // Paper API
        //Library paperApi = Library.of("io.papermc", "paper-api", "1.21.11-R0.1-20260120.191825-59")
        //		.withSha1("223f4b673a6cefe155849a18d7a82b422bf45335")
        //		.overrideRepo("https://repo.papermc.io/repository/maven-snapshots/");
        
        Library paperApi = Library.of("io.papermc.paper", "paper-api", "26.2.build.110-stable")
        		.withSha1("c94ea2a3efbe687f70b8135934588c8ca575b75a")
        		.overrideRepo("https://repo.papermc.io/repository/maven-public/");

        List<Library> libraries = List.of(
        	paperApi,
        	// Paper API Libraries
        	Library.of("org.xerial", "sqlite-jdbc", "3.41.0.0", "86168d5ae9bfc54dab9c47cd6e1af751c1d15eb3"),
        	Library.of("com.mysql", "mysql-connector-j", "8.0.32", "41ec3f8cdaccf6c46a47d7cd628eeb59a926d9d4"),
        	Library.of("commons-lang", "commons-lang", "2.6", "0ce1edb914c94ebc388f086c6827e8bdeec71ac2"),
        	Library.of("org.apache.commons", "commons-collections4", "4.4", "62ebe7544cb7164d87e0637a2a6a2bdc981395e8"),
        	Library.of("commons-collections", "commons-collections", "3.2.1", "761ea405b9b37ced573d2df0d1e3a4e0f9edc668"),
        	Library.of("net.md-5", "bungeecord-chat", "1.21-R0.2", "64956ff493786f981a15697ce406fe39a2551692"),
        	// Adventure
        	Library.of("net.kyori", "adventure-api", adventureVersion, "3e2ef126f3e3c3456995643aa49767af3b39ac34"),
        	Library.of("net.kyori", "adventure-key", adventureVersion, "32cf2afc230c0a932c71c30a86762246f23f345d") ,
        	Library.of("net.kyori", "adventure-text-serializer-gson", adventureVersion, "64921b6da90b2b4aa42e09342e12fd048783749f") ,
        	Library.of("net.kyori", "adventure-text-serializer-json", adventureVersion, "5afc1c7538e3625fb5d87926c20a96b428881e6d") ,
        	Library.of("net.kyori", "adventure-text-serializer-commons", adventureVersion, "bd00ab0ec93e5a326a0d9ce48b27c7025ce3a760") ,
        	Library.of("net.kyori", "adventure-text-serializer-legacy", adventureVersion, "29351ad8bac77a694aec074a63d89c3af08a1ada") ,
        	Library.of("net.kyori", "adventure-text-serializer-plain", adventureVersion, "eb0d8304dd9457246b6d4cae6c333e0a29375305") ,
        	Library.of("net.kyori", "adventure-text-minimessage", adventureVersion, "8435e812c70784ba7ccbd46210dccbcc576a18d5") ,
        	Library.of("net.kyori", "adventure-text-logger-slf4j", adventureVersion, "0efddd8e1faa2edae2d948b5d10be1a8f35c817a") ,
        	Library.of("net.kyori", "option", "1.1.0", "593fecb9c42688eebc7d8da5d6ea127f4d4c92a2"),
        	
        	// Complete Maven & Resolver Stack
        	Library.of("org.apache.maven", "maven-artifact", "3.9.6", "fb0979832c10c1a25d038a33ca862bef055fcdc8"),
        	Library.of("org.apache.maven", "maven-builder-support", "3.9.6", "bcfc9d8175eaba21111edf21e0355a8523461abc"),
        	Library.of("org.apache.maven", "maven-model", "3.9.6", "ac9a1c8a8cfa36f3a5489837e653ec0cd530d576"),
        	Library.of("org.apache.maven", "maven-model-builder", "3.9.6", "983ce00d50a9f78ad1b805e21e4fd71807fa6ebf"),
        	Library.of("org.apache.maven", "maven-resolver-provider", "3.9.6", "848c45d334f6cc5c8dd602b0e58fd4482964eddc"),
        	Library.of("org.apache.maven.resolver", "maven-resolver-api", "1.9.18", "0cd5174d6e80175398debe4869d484169c0abbf8"),
        	Library.of("org.apache.maven.resolver", "maven-resolver-connector-basic", "1.9.18", "baac1ca4eb5e5fbdd2df554262a1b97f84ae3cec"),
        	Library.of("org.apache.maven.resolver", "maven-resolver-impl", "1.9.18", "e928b128d1e52e6299f94431ce3df74647bc8c26"),
        	Library.of("org.apache.maven.resolver", "maven-resolver-named-locks", "1.9.18", "31f948d89dcb3d9739e70d5e1000ebd68eb4405d"),
        	Library.of("org.apache.maven.resolver", "maven-resolver-spi", "1.9.18", "7fa176b3353ef6d78d02db39e025f3c27a983158"),
        	Library.of("org.apache.maven.resolver", "maven-resolver-supplier", "1.9.18", "c1df8c4468f08dc237f49a7b4a08401d6d57b208"),
        	Library.of("org.apache.maven.resolver", "maven-resolver-transport-file", "1.9.18", "f7d4e607e0f245647f2ba59245de24ecba8a9946"),
        	Library.of("org.apache.maven.resolver", "maven-resolver-transport-http", "1.9.18", "61a5512ff44502a5b22800f097f43281cb934a72"),
        	Library.of("org.apache.maven.resolver", "maven-resolver-util", "1.9.18", "5ae9406f188ae4a999c353fce3fd77273797a216"),
        	Library.of("org.codehaus.plexus", "plexus-interpolation", "1.27", "8dc73f4ff5eafcbb7ec035ba54736e828b272533"),
        	Library.of("org.codehaus.plexus", "plexus-utils", "3.5.1", "c6bfb17c97ecc8863e88778ea301be742c62b06d")
        );

        // Set WorldEdit adapter class name here
        // as this provides more verbose stacktraces.
        // System.setProperty("worldedit.bukkit.adapter", "com.sk89q.worldedit.bukkit.adapter.impl.v1_21_11.PaperweightAdapter");

        return libraries;
    }

	/**
	 * Runs a new LibraryManager with the {@link #getLibraries()} list,
	 */
	public static void loadLibs() {
    	List<Library> libraries = getLibraries();

    	LibraryManager man = new LibraryManager("lib", true, 2, libraries);
    	man.run();
    }

    /**
     * Add a jar file to Fabric's Knot Classloader.
     * 
     * @implSpec If Dev Env, will skip adding, assuming already in dev classpath.
     * @implNote If debug print is True, file name will be logged.
     * @return True, or False if Exception thrown.
     */
    public static boolean propose(File file) {
        try {
        	if (!FabricLoader.getInstance().isDevelopmentEnvironment()) {
            	net.fabricmc.loader.impl.launch.FabricLauncherBase.getLauncher().addToClassPath(file.toPath(), LibraryManager.readPackagesFromJar(file));
            }

            if (CardboardConfig.DEBUG_VERBOSE_CALLS) {
            	LibraryManager.logger.info("Debug: Loading library " + file.getName());
            }
            return true;
        } catch (Exception e) {
            LibraryManager.logger.error("ERR: \"" + e.getMessage() + "\" while accessing Fabric Loader.");
            return false;
        }
    }

}